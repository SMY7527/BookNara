package com.booknara.booknaraPrj.client.aladin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AladinClient {

    private final AladinProperties aladinProperties;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://www.aladin.co.kr/ttb/api")
            .build();

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Long> cooldownUntil = new ConcurrentHashMap<>();
    private final ThreadLocal<Integer> lastKeyIndex = new ThreadLocal<>();

    public Integer getLastKeyIndex() {
        return lastKeyIndex.get();
    }

    public void clearLastKeyIndex() {
        lastKeyIndex.remove();
    }

    public void cooldownKey(int keyIndex, long cooldownMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, System.currentTimeMillis() + cooldownMs);
    }

    public void cooldownKeyUntil(int keyIndex, long cooldownUntilEpochMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, cooldownUntilEpochMs);
    }

    private String selectKey() {
        List<String> keys = aladinProperties.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("Aladin API keys not configured");
        }

        int size = keys.size();
        long now = System.currentTimeMillis();

        for (int tries = 0; tries < size; tries++) {
            int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
            long until = cooldownUntil.getOrDefault(idx, 0L);

            if (now >= until) {
                lastKeyIndex.set(idx);
                return keys.get(idx);
            }
        }

        int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
        lastKeyIndex.set(idx);
        return keys.get(idx);
    }

    /**
     * 알라딘 ItemLookUp API를 1회 호출해서 RAW(XML/JSON) 문자열로 반환
     * - 응답 상태 분류/파싱은 Service에서 수행
     */
    public String searchByIsbnOnceRaw(String isbn13) {
        String ttbKey = selectKey();


        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ItemLookUp.aspx")
                            .queryParam("ttbkey", ttbKey)
                            .queryParam("itemIdType", "ISBN13")
                            .queryParam("ItemId", isbn13)
                            // Output은 JS로 요청하되, 에러 시 XML이 내려올 수도 있으니 RAW로 받는다.
                            .queryParam("Output", "JS")
                            .queryParam("Version", "20131101")
                            .queryParam("Cover", "Big")
                            .build())
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw e;
        }


    }
}
