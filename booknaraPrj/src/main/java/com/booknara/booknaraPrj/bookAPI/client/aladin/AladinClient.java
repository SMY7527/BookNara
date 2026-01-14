package com.booknara.booknaraPrj.bookAPI.client.aladin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [AladinClient]
 * 알라딘 API 통신 및 다중 키 로테이션/관리 담당
 */
@Component
@RequiredArgsConstructor
public class AladinClient {

    private final AladinProperties aladinProperties;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://www.aladin.co.kr/ttb/api")
            .build();

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);             // 키 순환 인덱스
    private final ConcurrentHashMap<Integer, Long> cooldownUntil = new ConcurrentHashMap<>(); // 키별 쿨다운 종료 시각
    private final ThreadLocal<Integer> lastKeyIndex = new ThreadLocal<>();      // 현재 스레드에서 사용한 키 식별

    // --- 키 상태 관리 메소드 ---
    public Integer getLastKeyIndex() { return lastKeyIndex.get(); }
    public void clearLastKeyIndex() { lastKeyIndex.remove(); }

    /** 특정 키에 쿨다운(대기 시간) 설정 (요청 한도 초과 등 발생 시 호출) */
    public void cooldownKey(int keyIndex, long cooldownMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, System.currentTimeMillis() + cooldownMs);
    }

    public void cooldownKeyUntil(int keyIndex, long cooldownUntilEpochMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, cooldownUntilEpochMs);
    }

    /** 현재 즉시 사용 가능한 키가 있는지 확인 */
    public boolean hasAvailableKey() {
        List<String> keys = aladinProperties.getKeys();
        int size = (keys == null) ? 0 : keys.size();
        if (size <= 0) return false;

        long now = System.currentTimeMillis();
        for (int idx = 0; idx < size; idx++) {
            if (now >= cooldownUntil.getOrDefault(idx, 0L)) return true;
        }
        return false;
    }

    /** 다음 사용 가능 키가 풀리는 시각(Epoch MS) 반환 */
    public long nextAvailableAt() {
        List<String> keys = aladinProperties.getKeys();
        int size = (keys == null) ? 0 : keys.size();
        if (size <= 0) return 0L;

        long now = System.currentTimeMillis();
        long minUntil = Long.MAX_VALUE;
        boolean anyAvailableNow = false;

        for (int idx = 0; idx < size; idx++) {
            long until = cooldownUntil.getOrDefault(idx, 0L);
            if (now >= until) { anyAvailableNow = true; break; }
            minUntil = Math.min(minUntil, until);
        }
        return anyAvailableNow ? now : (minUntil == Long.MAX_VALUE ? now : minUntil);
    }

    /** 라운드 로빈 방식으로 쿨다운 상태가 아닌 키 선택 */
    private String selectKey() {
        List<String> keys = aladinProperties.getKeys();
        int size = keys.size();
        long now = System.currentTimeMillis();

        for (int tries = 0; tries < size; tries++) {
            int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
            if (now >= cooldownUntil.getOrDefault(idx, 0L)) {
                lastKeyIndex.set(idx);
                return keys.get(idx);
            }
        }
        throw new IllegalStateException("모든 알라딘 API 키의 요청 한도를 소진했습니다.");
    }

    /**
     * 알라딘 도서 상세 조회 API 호출 (Raw 데이터 반환)
     * 파싱 및 에러 분류는 상위 서비스 레이어에서 담당
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