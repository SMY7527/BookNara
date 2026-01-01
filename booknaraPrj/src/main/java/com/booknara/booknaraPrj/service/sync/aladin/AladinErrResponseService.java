package com.booknara.booknaraPrj.service.sync.aladin;

import com.booknara.booknaraPrj.client.aladin.AladinClient;
import com.booknara.booknaraPrj.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.client.aladin.AladinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AladinErrResponseService {

    private final AladinClient aladinClient;

    /**
     * 알라딘 ISBN 조회
     * - 200 OK / item 없음: null
     * - 429 : 다음 키로 재시도
     * - 5xx: 다음 키로 재시도
     * - 401/403: 인증/권한 문제 -> 즉시 실패(재시도 의미 적음)
     * - 기타 4xx: 즉시 실패
     */
    public AladinDTO searchByIsbnWithPolicy(String isbn13) {
        if (isbn13 == null || isbn13.isBlank()) return null;

        // 보통 키가 20개면 20번 내에서 해결됨.
        // (키 개수를 Service가 몰라도, 넉넉히 잡아두면 됨)
        final int maxAttempts = 25;

        RestClientResponseException lastHttpException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                AladinResponse response = aladinClient.searchByIsbnOnce(isbn13);

                // AladinResponse.item (List<AladinDTO>)
                List<AladinDTO> items = (response == null) ? null : response.getItem();
                if (items == null || items.isEmpty()) {
                    return null; // 검색 결과 없음
                }
                return items.get(0);

            } catch (RestClientResponseException e) {
                lastHttpException = e;

                int statusCode = e.getStatusCode().value();

                if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    log.warn("Aladin 429. retry next key. attempt={}/{}, isbn13={}", attempt, maxAttempts, isbn13);
                    continue;
                }

                if (statusCode >= 500 && statusCode <= 599) {
                    log.warn("Aladin 5xx. retry next key. attempt={}/{}, isbn13={}, status={}",
                            attempt, maxAttempts, isbn13, statusCode);
                    continue;
                }

                if (statusCode == HttpStatus.UNAUTHORIZED.value() || statusCode == HttpStatus.FORBIDDEN.value()) {
                    // 키/권한 문제로 재시도해도 의미가 적을 확률이 큼
                    log.error("Aladin auth error (401/403). isbn13={}, status={}, body={}",
                            isbn13, statusCode, safeBody(e));
                    throw e;
                }

                // 그 외 4xx는 보통 요청 파라미터 문제/차단 등 -> 즉시 실패
                log.error("Aladin 4xx fatal. isbn13={}, status={}, body={}",
                        isbn13, statusCode, safeBody(e));
                throw e;

            } catch (Exception e) {
                // HTTP가 아닌 예외(역직렬화 오류 등) -> 재시도 의미 적음
                log.error("Aladin non-http fatal error. isbn13={}", isbn13, e);
                throw e;
            }
        }

        // 여기까지 왔으면 429/5xx로 계속 실패한 것
        if (lastHttpException != null) {
            log.error("Aladin failed after retries. isbn13={}, lastStatus={}, body={}",
                    isbn13,
                    lastHttpException.getStatusCode().value(),
                    safeBody(lastHttpException)
            );
            throw lastHttpException;
        }

        throw new IllegalStateException("Aladin failed after retries. isbn13=" + isbn13);
    }

    private String safeBody(RestClientResponseException e) {
        try {
            String body = e.getResponseBodyAsString();
            if (body == null) return "";
            return body.length() > 300 ? body.substring(0, 300) + "..." : body;
        } catch (Exception ignore) {
            return "";
        }
    }
}
