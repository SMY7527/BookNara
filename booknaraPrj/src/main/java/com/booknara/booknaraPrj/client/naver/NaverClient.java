package com.booknara.booknaraPrj.client.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class NaverClient {

    private final NaverProperties naverProperties;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://openapi.naver.com")
            .build();

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    /** keyIndex -> cooldownUntilEpochMs */
    private final ConcurrentHashMap<Integer, Long> cooldownUntil = new ConcurrentHashMap<>();

    /** 이번 스레드에서 마지막으로 선택된 keyIndex */
    private final ThreadLocal<Integer> lastKeyIndex = new ThreadLocal<>();

    /** Service가 429 처리 시, 어떤 키였는지 조회 */
    public Integer getLastKeyIndex() {
        return lastKeyIndex.get();
    }

    /** keyIndex를 ms 동안 제외 */
    public void cooldownKey(int keyIndex, long cooldownMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, System.currentTimeMillis() + cooldownMs);
    }

    /** keyIndex를 특정 시각(epochMs)까지 제외 */
    public void cooldownKeyUntil(int keyIndex, long cooldownUntilEpochMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, cooldownUntilEpochMs);
    }

    private NaverProperties.Client selectClient() {
        List<NaverProperties.Client> clients = naverProperties.getClients();
        if (clients == null || clients.isEmpty()) {
            throw new IllegalStateException("Naver API clients not configured");
        }

        int size = clients.size();
        long now = System.currentTimeMillis();

        long minUntil = Long.MAX_VALUE;
        int fallbackIdx = -1;

        for (int tries = 0; tries < size; tries++) {
            int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
            long until = cooldownUntil.getOrDefault(idx, 0L);

            if (now >= until) {
                lastKeyIndex.set(idx);
                return clients.get(idx);
            }

            // 모두 쿨다운일 때 대비: 가장 빨리 풀리는 키 기록
            if (until < minUntil) {
                minUntil = until;
                fallbackIdx = idx;
            }
        }

        //  전부 쿨다운이면: 가장 빨리 풀리는 키까지 짧게 대기 후 그 키 사용
        if (fallbackIdx >= 0 && minUntil != Long.MAX_VALUE) {
            long sleepMs = Math.min(Math.max(minUntil - now, 50), 500); // 50~500ms만 대기
            try { Thread.sleep(sleepMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            lastKeyIndex.set(fallbackIdx);
            return clients.get(fallbackIdx);
        }

        // 최후 fallback
        int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
        lastKeyIndex.set(idx);
        return clients.get(idx);
    }


    /**
     * 네이버 book 검색 API를 1회 호출해서 DTO로 반환
     * - 재시도/백오프/응답 정책은 Service에서 수행
     */
    public NaverResponse searchByIsbnOnce(String isbn13) {
        NaverProperties.Client client = selectClient();

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/book.json")
                            .queryParam("query", isbn13)
                            .queryParam("display", 1)
                            .build())
                    .header("X-Naver-Client-Id", client.getId())
                    .header("X-Naver-Client-Secret", client.getSecret())
                    .retrieve()
                    .body(NaverResponse.class);

        } catch (RestClientResponseException e) {
            throw e;
        }
    }

    /** Service에서 keyIdx 읽고 난 뒤 정리 (ThreadLocal 오염 방지) */
    public void clearLastKeyIndex() {
        lastKeyIndex.remove();
    }
}
