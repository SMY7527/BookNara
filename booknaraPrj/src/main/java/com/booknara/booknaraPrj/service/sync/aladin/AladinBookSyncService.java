package com.booknara.booknaraPrj.service.sync.aladin;

import com.booknara.booknaraPrj.service.batch.hash.BookIsbnHash;
import com.booknara.booknaraPrj.client.aladin.AladinClient;
import com.booknara.booknaraPrj.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.service.policy.TempReadyPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AladinBookSyncService {

    private final BookBatchMapper batchMapper;
    private final AladinClient aladinClient;
    private final TempReadyPolicy readyPolicy;

    // 튜닝 포인트
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF_MS = 200;

    /**
     * NOTREADY(0) 중 알라딘 보강이 필요한 ISBN을 limit 단위로 처리
     * - pubdate / genreId / aladinImageBig 보강
     * - DB 재조회 후 READY 조건 충족 시 STATUS_CD=1로 승격
     */
    public int syncOnce(int limit) {
        List<String> targetIsbnList = batchMapper.selectTempIsbnForAladin(limit);

        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("aladin sync: no targets");
            return 0;
        }

        int successCount = 0;
        for (String isbn13 : targetIsbnList) {
            if (syncOne(isbn13)) {
                successCount++;
            }
        }

        log.info("aladin sync: success={}/{}", successCount, targetIsbnList.size());
        return successCount;
    }

    /**
     * 더 이상 처리 대상이 없을 때까지 반복
     */
    public void syncLoop(int limit) {
        while (true) {
            int successCount = syncOnce(limit);
            if (successCount == 0) break;
        }
        log.info("aladin syncLoop finished");
    }

    @Transactional
    public boolean syncOne(String isbn13) {
        try {
            AladinResponse apiResponse = fetchWithRetry(isbn13);

            // 1) API 응답 -> TEMP 업데이트용 DTO 변환
            BookIsbnTempDTO tempUpdate = toTempUpdateDto(isbn13, apiResponse);

            // 2) TEMP 업데이트
            int updatedRows = batchMapper.updateTempFromAladin(tempUpdate);
            if (updatedRows == 0) {
                log.warn("aladin sync: update skipped isbn13={}", isbn13);
                return false;
            }

            // 3) DB 재조회
            BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
            if (mergedTemp == null) return false;

            // 4) READY 판정
            if (readyPolicy.isReady(mergedTemp)) {
                // ✅ READY 될 때만 해시 계산/저장
                String newHash = BookIsbnHash.compute(mergedTemp);
                batchMapper.updateTempDataHash(isbn13, newHash);
                batchMapper.markTempReady(isbn13);
            }


            return true;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("aladin sync interrupted isbn13={}", isbn13, ie);
            return false;

        } catch (Exception ex) {
            log.warn("aladin sync failed isbn13={}", isbn13, ex);
            return false;
        }
    }


    /**
     * 429/403 발생 시 지수 백오프 + 지터로 재시도
     */
    private AladinResponse fetchWithRetry(String isbn13) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                // ⚠️ 네 프로젝트 AladinClient 실제 메서드명에 맞춰주세요.
                // 예: aladinClient.searchByIsbnOnce(isbn13) / aladinClient.lookupByIsbnOnce(isbn13)
                return aladinClient.searchByIsbnOnce(isbn13);

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                if (statusCode == 429 || statusCode == 403) {
                    long jitterMs = (long) (Math.random() * 150);

                    log.warn("aladin rate-limited isbn13={} attempt={} status={} backoff={}ms",
                            isbn13, attempt, statusCode, backoffMs);

                    Thread.sleep(backoffMs + jitterMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                throw ex;
            }
        }

        throw new RuntimeException("aladin retry exhausted isbn13=" + isbn13);
    }

    /**
     * 알라딘 응답을 TEMP 업데이트용 DTO로 변환
     * - 프로젝트 AladinDTO: pubdate(String), cover(String), categoryId(String)
     */
    private BookIsbnTempDTO toTempUpdateDto(String isbn13, AladinResponse apiResponse) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setAladinFetchedAt(LocalDateTime.now());

        if (apiResponse == null || apiResponse.getItem() == null || apiResponse.getItem().isEmpty()) {
            return updateDto;
        }

        AladinDTO firstItem = apiResponse.getItem().get(0);

        updateDto.setPubdate(parsePubdate(firstItem.getPubdate()));
        updateDto.setGenreId(parseInteger(firstItem.getCategoryId()));
        updateDto.setAladinImageBig(normalizeText(firstItem.getCover()));
        updateDto.setDescription(normalizeText(firstItem.getDescription()));

        return updateDto;
    }

    /**
     * 알라딘 pubdate 포맷 대응:
     * - "2016-05-02" (ISO)
     * - "20160502"   (compact) 같은 케이스도 방어
     */
    private LocalDate parsePubdate(String pubdate) {
        String value = normalizeText(pubdate);
        if (value == null) return null;

        try {
            // 1) 2016-05-02
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignore) {}

        try {
            // 2) 20160502
            DateTimeFormatter compact = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(value, compact);
        } catch (DateTimeParseException ignore) {}

        // 못 읽으면 null 처리 (READY 조건에서 걸러짐)
        return null;
    }

    private Integer parseInteger(String numberText) {
        String value = normalizeText(numberText);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
