package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.policy;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinClient;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model.AladinCallResult;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.parser.AladinPayloadParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * [AladinFetchPolicy]
 * 알라딘 API 호출 시의 재시도 정책 및 에러 핸들링을 전담하는 정책 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AladinFetchPolicy {

    private final AladinClient aladinClient;
    private final AladinPayloadParser payloadParser;
    private final AladinRateLimitManager rateLimitManager;

    private static final int MAX_RETRY_COUNT = 3;       // 최대 재시도 횟수
    private static final long INITIAL_BACKOFF_MS = 200; // 초기 대기 시간 (지수 백오프 적용)

    /**
     * 재시도 로직을 포함한 알라딘 도서 정보 수집 수행
     * 1. 사용 가능한 키 확인
     * 2. HTTP 호출 및 응답 분석 (XML/JSON)
     * 3. 429(Rate Limit), 5xx(Server Error) 발생 시 백오프 대기 후 재시도
     * 4. 일일 한도 초과(에러코드 10) 시 해당 키 쿨다운 및 루프 중단 결정
     */
    public AladinCallResult fetchWithRetry(String isbn13) throws InterruptedException {
        // [사전 체크] 즉시 사용 가능한 API 키가 하나도 없으면 오늘 수집 중단
        if (!aladinClient.hasAvailableKey()) {
            return AladinCallResult.stopForToday("ALL_KEYS_UNAVAILABLE");
        }

        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            Integer keyIdx = null;

            try {
                // 1) API 호출 및 사용된 키 인덱스 확보
                String raw = aladinClient.searchByIsbnOnceRaw(isbn13);
                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // (A) XML 응답 처리 (알라딘 에러는 대개 XML로 수신됨)
                if (payloadParser.looksLikeXml(raw)) {
                    if (payloadParser.looksLikeXmlError(raw)) {
                        int errorCode = payloadParser.parseXmlErrorCode(raw);

                        // 에러코드 10: 일일 호출 한도 초과 (Key 별 쿨다운 처리)
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

                    // 비정상적 XML 응답 시 지연 후 재시도
                    log.warn("aladin got xml but not <error>. isbn13={} attempt={}", isbn13, attempt);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // (B) JSON/JS 응답 처리 및 파싱
                AladinCallResult parsed = payloadParser.parseJsonToResult(isbn13, raw);

                // JSON 응답 내 에러코드 10 처리
                if (keyIdx != null && "10".equals(parsed.getErrorCode())) {
                    rateLimitManager.onDailyLimit10(keyIdx, isbn13, "JSON 10");

                    if (!aladinClient.hasAvailableKey()) {
                        return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_10");
                    }
                    return parsed;
                }

                // 호출 성공 시 해당 키의 429 누적 카운트 리셋
                if (keyIdx != null && parsed.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                    rateLimitManager.reset429OnSuccess(keyIdx);
                }

                return parsed;

            } catch (IllegalStateException ex) {
                // selectKey()에서 사용 가능한 키가 없을 때 발생
                log.warn("aladin keys exhausted -> stopForToday. msg={}", ex.getMessage());
                return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED");

            } catch (RestClientResponseException ex) {
                // HTTP 에러 상태 코드별 대응
                int statusCode = ex.getStatusCode().value();
                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // [HTTP 429] 너무 많은 요청 (Rate Limit Hit)
                if (statusCode == 429) {
                    rateLimitManager.on429(keyIdx, isbn13);

                    if (!aladinClient.hasAvailableKey()) {
                        return AladinCallResult.stopForToday("ALL_KEYS_EXHAUSTED_429");
                    }

                    log.warn("aladin 429 rate-limited isbn13={} attempt={}/{}", isbn13, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000); // 지수 백오프 적용
                    continue;
                }

                // [HTTP 5xx] 서버측 오류 발생 시 재시도
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

                // [HTTP 401/403] 권한 문제 등 재시도가 무의미한 케이스
                if (statusCode == 401 || statusCode == 403) {
                    log.error("aladin auth/forbidden isbn13={} status={}", isbn13, statusCode);
                    return AladinCallResult.nonRetryFail("HTTP_" + statusCode);
                }

                log.warn("aladin non-retry client error isbn13={} status={}", isbn13, statusCode);
                return AladinCallResult.nonRetryFail("HTTP_" + statusCode);

            } catch (Exception ex) {
                // 예상치 못한 예외 발생 시 로그 기록 및 재시도
                log.warn("aladin unexpected error isbn13={} attempt={}/{} msg={}",
                        isbn13, attempt, MAX_RETRY_COUNT, ex.getMessage());

                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 3000);
            }
        }

        return AladinCallResult.retryableFail("RETRY_EXHAUSTED");
    }
}