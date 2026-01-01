package com.booknara.booknaraPrj.service.sync.naver;

import com.booknara.booknaraPrj.client.naver.NaverClient;
import com.booknara.booknaraPrj.client.naver.NaverDTO;
import com.booknara.booknaraPrj.client.naver.NaverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverErrResponseService {

    private final NaverClient naverClient;

    /**
     * ISBN으로 네이버 조회를 시도하고, 상황별로 대응.
     *
     * 대응 정책:
     * - 200(성공): DTO 반환 (단, items 비었으면 null)
     * - 429(쿼터): 다른 키로 재시도 (최대 n회)
     * - 401/403(인증/차단): 재시도하지 말고 즉시 실패 처리
     * - 5xx(네이버 장애): 재시도하지 않고 skip (배치 다음 회차에 기대)
     * - 네트워크/기타: skip
     */
    public NaverDTO searchByIsbnWithPolicy(String isbn13) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                NaverResponse response = naverClient.searchByIsbnOnce(isbn13);

                if (response== null || response.getItems() == null || response.getItems().isEmpty()) {
                    return null; // 결과 없음은 정상
                }
                return response.getItems().get(0);

            } catch (RestClientResponseException e) {
                int status = e.getRawStatusCode();

                if (status == 429) { //  다음 키로 재시도
                    log.warn("[NAVER] 429 Too Many Requests. attempt={}/{}", attempt, maxAttempts);
                    continue;
                }
                if (status == 401 || status == 403) { // 인증/차단
                    log.error("[NAVER] auth/forbidden status={}", status);
                    return null;
                }
                if (status >= 500) { // 서버 오류
                    log.warn("[NAVER] server error status={}", status);
                    return null;
                }
                log.warn("[NAVER] client error status={}", status);
                return null;

            } catch (Exception e) {
                log.warn("[NAVER] unexpected error - skip. msg={}", e.getMessage());
                return null;
            }
        }
        return null;
    }

}
