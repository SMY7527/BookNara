package com.booknara.booknaraPrj.service.sync.naver;

import com.booknara.booknaraPrj.client.naver.NaverClient;
import com.booknara.booknaraPrj.client.naver.NaverDTO;
import com.booknara.booknaraPrj.client.naver.NaverResponse;
import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.domain.ResponseStatus;
import com.booknara.booknaraPrj.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.service.policy.TempReadyPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    // keyIndex별 429 누적 횟수 (너의 기존 정책 유지)
    private final ConcurrentHashMap<Integer, Integer> daily429Count = new ConcurrentHashMap<>();
    private static final int DAILY_429_COUNT_LIMIT = 3;

    /** 429 발생 시 해당 API Key를 잠시 제외 */
    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // 15초

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5;

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
        return targetIsbnList.size();
    }

    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break;
        }
        log.info("naver syncLoop finished");
    }

    /**
     * 네이버 API 보강 (단일 ISBN)
     * - NAVER_FETCHED_AT: 시도하면 무조건 기록
     * - NAVER_RES_STATUS: 응답 결과에 따라 기록
     * - 데이터가 있으면 authors/description/image 업데이트
     */
    @Transactional
    public boolean syncOne(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            NaverCallResult result = fetchWithRetry(isbn13);

            // 1) 시도 시각 + 상태 저장은 "항상" (핵심)
            batchMapper.updateTempNaverMeta(
                    isbn13,
                    triedAt,
                    result.status.getCode()
            );

            // 2) 데이터가 있는 성공일 때만 실제 데이터 업데이트
            if (result.status == ResponseStatus.SUCCESS_WITH_DATA) {
                BookIsbnTempDTO tempUpdate = toTempUpdateDto(isbn13, result.response, triedAt);
                batchMapper.updateTempFromNaver(tempUpdate);
            }

            // 3) READY 판정 (최종 상태 기준)
            BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
            if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
                batchMapper.markTempReady(isbn13);
            }

            return result.status == ResponseStatus.SUCCESS_WITH_DATA;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("naver sync interrupted isbn13={}", isbn13);
            return false;

        } catch (Exception ex) {
            // 여기까지 온 예외는 "정상적인 HTTP 에러 외" 케이스들
            log.warn("naver sync failed (unexpected) isbn13={}", isbn13, ex);

            // 예외라도 "시도"는 했으니 상태를 retryable로 박아두는게 보통 안전
            try {
                batchMapper.updateTempNaverMeta(
                        isbn13,
                        triedAt,
                        ResponseStatus.RETRYABLE_FAIL.getCode()
                );
            } catch (Exception ignore) {}

            return false;

        } finally {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 네이버 호출 + 정책 적용
     * - 200 + items 있음  -> SUCCESS_WITH_DATA
     * - 200 + items 없음  -> SUCCESS_NO_DATA  (데이터 문제)
     * - 429 / 5xx / 네트워크 -> RETRYABLE_FAIL
     * - 401/403/400 등     -> NONRETRY_FAIL
     */
    private NaverCallResult fetchWithRetry(String isbn13) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                NaverResponse resp = naverClient.searchByIsbnOnce(isbn13);

                // 200 OK 이지만 데이터 없는 경우
                if (resp == null || resp.getItems() == null || resp.getItems().isEmpty()) {
                    return NaverCallResult.noData();
                }

                return NaverCallResult.withData(resp);

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                Integer keyIdx = naverClient.getLastKeyIndex();
                naverClient.clearLastKeyIndex();

                // 429: 재시도 가능 + 키 쿨다운
                if (statusCode == 429) {
                    if (keyIdx != null) {
                        int count = daily429Count.merge(keyIdx, 1, Integer::sum);

                        if (count >= DAILY_429_COUNT_LIMIT) {
                            long until = getNextScheduledTime();
                            naverClient.cooldownKeyUntil(keyIdx, until);
                            daily429Count.remove(keyIdx);

                            log.warn("naver key exhausted keyIdx={} until={} isbn13={}", keyIdx, until, isbn13);
                            return NaverCallResult.retryableFail();
                        }

                        naverClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
                    }

                    log.warn("naver 429 rate-limited isbn13={} attempt={}/{}", isbn13, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 5xx: 서버 장애 -> 재시도 가능
                if (statusCode >= 500 && statusCode <= 599) {
                    log.warn("naver 5xx server error isbn13={} status={} attempt={}/{}", isbn13, statusCode, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 401/403: 인증/차단 -> 재시도 불가
                if (statusCode == 401 || statusCode == 403) {
                    log.error("naver auth/forbidden isbn13={} status={}", isbn13, statusCode);
                    return NaverCallResult.nonRetryFail();
                }

                // 400/404 등: 요청 문제 가능성 -> 재시도 불가(보통)
                log.warn("naver non-retry client error isbn13={} status={}", isbn13, statusCode);
                return NaverCallResult.nonRetryFail();

            } catch (Exception ex) {
                // 네트워크/파싱 등 -> 재시도 가능
                log.warn("naver unexpected error isbn13={} attempt={}/{} msg={}", isbn13, attempt, MAX_RETRY_COUNT, ex.getMessage());
                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 3000);
            }
        }

        return NaverCallResult.retryableFail();
    }

    /**
     * resp -> temp dto 변환 (성공+데이터 있을 때만 호출됨)
     */
    private BookIsbnTempDTO toTempUpdateDto(String isbn13, NaverResponse apiResponse, LocalDateTime triedAt) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setNaverFetchedAt(triedAt);

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

    private long getNextScheduledTime() {
        return ZonedDateTime.now(KST_ZONE)
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(KST_ZONE)
                .plusMinutes(DELAY_MINUTES)
                .toInstant()
                .toEpochMilli();
    }

    // =========================================================
    // 내부 결과 객체: status + response
    // =========================================================
    private static class NaverCallResult {
        final ResponseStatus status;
        final NaverResponse response;

        private NaverCallResult(ResponseStatus status, NaverResponse response) {
            this.status = status;
            this.response = response;
        }

        static NaverCallResult withData(NaverResponse response) {
            return new NaverCallResult(ResponseStatus.SUCCESS_WITH_DATA, response);
        }

        static NaverCallResult noData() {
            return new NaverCallResult(ResponseStatus.SUCCESS_NO_DATA, null);
        }

        static NaverCallResult retryableFail() {
            return new NaverCallResult(ResponseStatus.RETRYABLE_FAIL, null);
        }

        static NaverCallResult nonRetryFail() {
            return new NaverCallResult(ResponseStatus.NONRETRY_FAIL, null);
        }
    }
}
