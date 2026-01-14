package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.policy;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinClient;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model.AladinCallResult;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.parser.AladinPayloadParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinFetchPolicy {

    private final AladinClient aladinClient;
    private final AladinPayloadParser payloadParser;
    private final AladinRateLimitManager rateLimitManager;

    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF_MS = 200;

    public AladinCallResult fetchWithRetry(String isbn13) throws InterruptedException {
        if (!aladinClient.hasAvailableKey()) {
            return AladinCallResult.stopForToday("ALL_KEYS_UNAVAILABLE");
        }

        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            Integer keyIdx = null;

            try {
                String raw = aladinClient.searchByIsbnOnceRaw(isbn13);
                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // (A) XML
                if (payloadParser.looksLikeXml(raw)) {
                    if (payloadParser.looksLikeXmlError(raw)) {
                        int errorCode = payloadParser.parseXmlErrorCode(raw);

                        if (errorCode == 10) {
                            rateLimitManager.onDailyLimit10(keyIdx, isbn13, "XML 10");

                            if (!aladinClient.hasAvailableKey()) {
                                return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_10");
                            }
                            return AladinCallResult.retryableFail("10");
                        }

                        log.warn("aladin xml error. errorCode={} isbn13={}", errorCode, isbn13);
                        return AladinCallResult.nonRetryFail(String.valueOf(errorCode));
                    }

                    log.warn("aladin got xml but not <error>. isbn13={} attempt={}", isbn13, attempt);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // (B) JSON/JS
                AladinCallResult parsed = payloadParser.parseJsonToResult(isbn13, raw);

                // JSON 10도 키 쿨다운 + 전부 불가면 stop
                if (keyIdx != null && "10".equals(parsed.getErrorCode())) {
                    rateLimitManager.onDailyLimit10(keyIdx, isbn13, "JSON 10");

                    if (!aladinClient.hasAvailableKey()) {
                        return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_10");
                    }
                    return parsed;
                }

                // 성공 시 429 누적 리셋
                if (keyIdx != null && parsed.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                    rateLimitManager.reset429OnSuccess(keyIdx);
                }

                return parsed;

            } catch (IllegalStateException ex) {
                log.warn("aladin keys exhausted -> stopForToday. msg={}", ex.getMessage());
                return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED");

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // 429
                if (statusCode == 429) {
                    rateLimitManager.on429(keyIdx, isbn13);

                    if (!aladinClient.hasAvailableKey()) {
                        return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_429");
                    }

                    log.warn("aladin 429 rate-limited isbn13={} attempt={}/{}", isbn13, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 5xx
                if (statusCode >= 500 && statusCode <= 599) {
                    rateLimitManager.on5xx(keyIdx);

                    if (!aladinClient.hasAvailableKey()) {
                        return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_5XX");
                    }

                    log.warn("aladin 5xx server error isbn13={} status={} attempt={}/{}",
                            isbn13, statusCode, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 401/403
                if (statusCode == 401 || statusCode == 403) {
                    log.error("aladin auth/forbidden isbn13={} status={}", isbn13, statusCode);
                    return AladinCallResult.nonRetryFail("HTTP_" + statusCode);
                }

                // 기타 4xx
                log.warn("aladin non-retry client error isbn13={} status={}", isbn13, statusCode);
                return AladinCallResult.nonRetryFail("HTTP_" + statusCode);

            } catch (Exception ex) {
                log.warn("aladin unexpected error isbn13={} attempt={}/{} msg={}",
                        isbn13, attempt, MAX_RETRY_COUNT, ex.getMessage());

                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 3000);
            }
        }

        return AladinCallResult.retryableFail("RETRY_EXHAUSTED");
    }
}
