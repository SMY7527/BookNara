package com.booknara.booknaraPrj.service.sync.naver;

import com.booknara.booknaraPrj.client.naver.NaverClient;
import com.booknara.booknaraPrj.client.naver.NaverDTO;
import com.booknara.booknaraPrj.client.naver.NaverResponse;
import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.service.policy.TempReadyPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverBookSyncService {

    private final BookBatchMapper batchMapper;
    private final NaverClient naverClient;
    private final TempReadyPolicy readyPolicy;

    // =========================
    // 튜닝 포인트
    // =========================
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF_MS = 200;

    /** 429 발생 시 해당 API Key를 잠시 제외 */
    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // 15초

    /**
     * NOTREADY(0) 중 네이버 보강이 필요한 ISBN을 limit 단위로 처리
     * - authors / description / naverImage 보강
     * - READY 조건 충족 시 STATUS_CD=1로 승격
     */
    public int syncOnce(int limit) {
        List<String> targetIsbnList = batchMapper.selectTempIsbnForNaver(limit);

        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("naver sync: no targets");
            return 0;
        }

        int successCount = 0;
        for (String isbn13 : targetIsbnList) {
            if (syncOne(isbn13)) {
                successCount++;
            }
        }

        log.info("naver sync: success={}/{}", successCount, targetIsbnList.size());

        // 종료 판단은 "성공 수"가 아니라 "대상 수" 기준이 더 안전
        return targetIsbnList.size();
    }

    /**
     * 더 이상 처리 대상이 없을 때까지 반복
     */
    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break;
        }
        log.info("naver syncLoop finished");
    }

    /**
     * 네이버 API를 이용해 단일 ISBN의 TEMP 데이터를 보강.
     *
     * 처리 흐름:
     * 1) 네이버 API 호출 (재시도 + 백오프 포함)
     * 2) 응답을 TEMP 업데이트용 DTO로 변환
     * 3) TEMP 테이블 업데이트
     * 4) DB 재조회 후 READY 조건 충족 시 STATUS_CD 승격
     *
     * 실패해도 예외를 전파하지 않음.
     *  - NOTREADY 상태 유지
     *  - 다음 스케줄에서 재시도 대상이 됨
     */
    @Transactional
    public boolean syncOne(String isbn13) {

        // 네이버 시도 시간은 성공/실패 관계없이 기록
        //    → "무한 재시도 루프" 방지 목적
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            // 1) 네이버 API 호출 (지수 백오프 + 재시도 포함)
            NaverResponse apiResponse = fetchWithRetry(isbn13);

            // 2) 응답 → TEMP 업데이트 DTO
            //    - 응답이 null이어도 fetchedAt(triedAt)은 기록해야 하므로 DTO는 항상 생성
            BookIsbnTempDTO tempUpdate =
                    (apiResponse == null)
                            ? buildTriedOnlyDto(isbn13, triedAt)   // 실패: 시도 기록만
                            : toTempUpdateDto(isbn13, apiResponse); // 성공: authors/description/image 보강

            // 실패 케이스에서도 naverFetchedAt은 강제로 기록
            tempUpdate.setNaverFetchedAt(triedAt);

            // 3) TEMP 업데이트
            int updatedRows = batchMapper.updateTempFromNaver(tempUpdate);
            if (updatedRows == 0) {
                log.warn("naver sync skipped (no row) isbn13={}", isbn13);
                return false;
            }

            // 4) DB 재조회 후 READY 판정
            //    - 네이버/알라딘/InfoNaru가 합쳐진 "최종 상태" 기준
            BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
            if (readyPolicy.isReady(mergedTemp)) {
                batchMapper.markTempReady(isbn13);
            }

            // apiResponse가 null이면 "실패지만 시도 기록은 남김"
            return apiResponse != null;

        } catch (InterruptedException ie) {
            // InterruptedException은 인터럽트 플래그 복구 필수
            Thread.currentThread().interrupt();
            log.warn("naver sync interrupted isbn13={}", isbn13);
            return false;

        } catch (Exception ex) {
            // 네이버 실패는 정상 흐름: NOTREADY 유지
            log.warn("naver sync failed isbn13={}", isbn13, ex);

            try {
                BookIsbnTempDTO triedOnly = buildTriedOnlyDto(isbn13, triedAt);
                batchMapper.updateTempFromNaver(triedOnly);
            } catch (Exception ignore) {
                // 배치 전체 중단 방지
            }

            return false;

        } finally {
            // 네이버 API 정책:
            // - 초당 10건 제한
            // - 안전 마진 두고 6~7 TPS 수준으로 제한
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private BookIsbnTempDTO buildTriedOnlyDto(String isbn13, LocalDateTime triedAt) {
        BookIsbnTempDTO dto = new BookIsbnTempDTO();
        dto.setIsbn13(isbn13);
        dto.setNaverFetchedAt(triedAt);
        return dto;
    }

    /**
     * 429 / 403 발생 시 지수 백오프 + 지터로 재시도
     *
     * - 429: 초당/일일 호출 한도 초과 가능성
     *        → 해당 API Key를 일정 시간 제외(cooldown)
     * - 401 / 403: 인증/권한 문제 가능성
     *        → 재시도 의미 없으므로 즉시 포기
     */
    private NaverResponse fetchWithRetry(String isbn13) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return naverClient.searchByIsbnOnce(isbn13);

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                // 이번 요청에 사용된 API Key index
                Integer keyIdx = naverClient.getLastKeyIndex();
                naverClient.clearLastKeyIndex();

                if (statusCode == 429) {
                    //  429면 "해당 키"를 잠시 제외
                    if (keyIdx != null) {
                        naverClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
                    }

                    long jitterMs = (long) (Math.random() * 150);

                    log.warn(
                            "naver rate-limited isbn13={} attempt={} keyIdx={} backoff={}ms cooldown={}ms",
                            isbn13, attempt, keyIdx, backoffMs, KEY_COOLDOWN_ON_429_MS
                    );

                    Thread.sleep(backoffMs + jitterMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 401/403 등은 retry 의미 적음 → 즉시 포기
                log.warn("naver request failed isbn13={} status={} keyIdx={}",
                        isbn13, statusCode, keyIdx);
                return null;
            }
        }

        log.warn("naver retry exhausted isbn13={}", isbn13);
        return null;
    }

    /**
     * 네이버 응답을 TEMP 업데이트용 DTO로 변환
     *
     * - 프로젝트 NaverDTO는 image / author / description만 제공
     * - 제목 / 출판사는 InfoNaru에서 채우므로
     *   네이버는 "보강" 역할만 수행
     */
    private BookIsbnTempDTO toTempUpdateDto(String isbn13, NaverResponse apiResponse) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setNaverFetchedAt(LocalDateTime.now());

        if (apiResponse == null || apiResponse.getItems() == null || apiResponse.getItems().isEmpty()) {
            return updateDto;
        }

        NaverDTO firstItem = apiResponse.getItems().get(0);

        updateDto.setAuthors(normalizeText(firstItem.getAuthor()));
        updateDto.setDescription(normalizeText(firstItem.getDescription()));
        updateDto.setNaverImage(normalizeText(firstItem.getImage()));

        return updateDto;
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
