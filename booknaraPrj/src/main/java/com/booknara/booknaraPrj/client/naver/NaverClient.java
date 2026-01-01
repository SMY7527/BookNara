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

        // 최대 size번까지 시도하면서 쿨다운 아닌 키를 선택
        for (int tries = 0; tries < size; tries++) {
            int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
            long until = cooldownUntil.getOrDefault(idx, 0L);

            if (now >= until) {
                lastKeyIndex.set(idx);
                return clients.get(idx);
            }
        }

        // 전부 쿨다운이면: 일단 다음 키를 반환(어쩔 수 없이)
        int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
        lastKeyIndex.set(idx);
        return clients.get(idx);
    }

    /** 네이버 book 검색 API를 1회 호출해서 DTO로 반환 (재시도/대응은 Service에서) */
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

    //Service에서 읽고 난 뒤 정리
    public void clearLastKeyIndex() {
        lastKeyIndex.remove();
    }
}
