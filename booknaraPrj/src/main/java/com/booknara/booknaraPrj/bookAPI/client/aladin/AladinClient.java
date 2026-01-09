package com.booknara.booknaraPrj.bookAPI.client.aladin;

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

    /**
     *  지금 당장 사용 가능한 키가 1개라도 있으면 true
     */
    public boolean hasAvailableKey() {
        List<String> keys = aladinProperties.getKeys();
        int size = (keys == null) ? 0 : keys.size();
        if (size <= 0) return false;

        long now = System.currentTimeMillis();
        for (int idx = 0; idx < size; idx++) {
            long until = cooldownUntil.getOrDefault(idx, 0L);
            if (now >= until) return true;
        }
        return false;
    }

    /**
     * (옵션) 다음으로 사용 가능한 키가 풀리는 시각(epoch ms)
     * - 모두 사용 가능이면 now 반환
     * - keys가 없으면 0 반환
     */
    public long nextAvailableAt() {
        List<String> keys = aladinProperties.getKeys();
        int size = (keys == null) ? 0 : keys.size();
        if (size <= 0) return 0L;

        long now = System.currentTimeMillis();

        long minUntil = Long.MAX_VALUE;
        boolean anyAvailableNow = false;

        for (int idx = 0; idx < size; idx++) {
            long until = cooldownUntil.getOrDefault(idx, 0L);
            if (now >= until) {
                anyAvailableNow = true;
                break;
            }
            minUntil = Math.min(minUntil, until);
        }

        return anyAvailableNow ? now : (minUntil == Long.MAX_VALUE ? now : minUntil);
    }

    private String selectKey() {
        List<String> keys = aladinProperties.getKeys();
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

        // 모든 키 요청한도 소진(또는 전부 쿨다운)
        throw new IllegalStateException("알라딘 API key들의 모든 일일 요청한도를 소진하였습니다.");
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
