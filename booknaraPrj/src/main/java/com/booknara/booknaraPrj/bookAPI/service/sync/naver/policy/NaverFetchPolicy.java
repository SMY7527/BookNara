package com.booknara.booknaraPrj.bookAPI.service.sync.naver.policy;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverClient;
import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.service.sync.naver.model.NaverCallResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverFetchPolicy {

    private final NaverClient naverClient;
    private final NaverRateLimitManager rateLimitManager;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF_MS = 200;

    /**
     * 네이버 호출 + 재시도 정책
     * - 200 + items 있음  -> SUCCESS_WITH_DATA
     * - 200 + items 없음  -> SUCCESS_NO_DATA
     * - 429 / 5xx / 네트워크 -> RETRYABLE_FAIL
     * - 401/403/400 등     -> NONRETRY_FAIL
     */
    public NaverCallResult fetchWithRetry(String isbn13) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                NaverResponse resp = naverClient.searchByIsbnOnce(isbn13);

                if (resp == null || resp.getItems() == null || resp.getItems().isEmpty()) {
                    return NaverCallResult.noData();
                }

                // (선택) 성공하면 429 누적 리셋하고 싶으면 활성화
                Integer keyIdx = naverClient.getLastKeyIndex();
                naverClient.clearLastKeyIndex();
                rateLimitManager.resetOnSuccess(keyIdx);

                return NaverCallResult.withData(resp);

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                Integer keyIdx = naverClient.getLastKeyIndex();
                naverClient.clearLastKeyIndex();

                if (statusCode == 429) {
                    rateLimitManager.on429(keyIdx, isbn13);

                    log.warn("naver 429 rate-limited isbn13={} attempt={}/{}", isbn13, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                if (statusCode >= 500 && statusCode <= 599) {
                    log.warn("naver 5xx server error isbn13={} status={} attempt={}/{}",
                            isbn13, statusCode, attempt, MAX_RETRY_COUNT);

                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                if (statusCode == 401 || statusCode == 403) {
                    log.error("naver auth/forbidden isbn13={} status={}", isbn13, statusCode);
                    return NaverCallResult.nonRetryFail();
                }

                log.warn("naver non-retry client error isbn13={} status={}", isbn13, statusCode);
                return NaverCallResult.nonRetryFail();

            } catch (Exception ex) {
                log.warn("naver unexpected error isbn13={} attempt={}/{} msg={}",
                        isbn13, attempt, MAX_RETRY_COUNT, ex.getMessage());

                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 3000);
            }
        }

        return NaverCallResult.retryableFail();
    }
}

