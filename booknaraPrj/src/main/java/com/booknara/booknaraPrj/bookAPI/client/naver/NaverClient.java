package com.booknara.booknaraPrj.bookAPI.client.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [NaverClient]
 * 네이버 도서 검색 API 통신 및 다중 계정 쿨다운/로테이션 관리
 */
@Component
@RequiredArgsConstructor
public class NaverClient {

    private final NaverProperties naverProperties;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://openapi.naver.com")
            .build();

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Long> cooldownUntil = new ConcurrentHashMap<>(); // 키별 제한 종료 시각
    private final ThreadLocal<Integer> lastKeyIndex = new ThreadLocal<>(); // 현재 스레드에서 사용 중인 키 번호

    /** API 에러 발생 시(429 등) 어떤 키가 문제였는지 서비스 레이어에서 확인용 */
    public Integer getLastKeyIndex() { return lastKeyIndex.get(); }

    /** 특정 키를 일정 시간(ms) 동안 사용 제외 목록에 추가 */
    public void cooldownKey(int keyIndex, long cooldownMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, System.currentTimeMillis() + cooldownMs);
    }

    public void cooldownKeyUntil(int keyIndex, long cooldownUntilEpochMs) {
        if (keyIndex < 0) return;
        cooldownUntil.put(keyIndex, cooldownUntilEpochMs);
    }

    /**
     * 사용할 네이버 클라이언트(ID/Secret)를 라운드 로빈 방식으로 선택
     * 모든 키가 쿨다운 중이면 가장 빨리 풀리는 키를 찾아 잠시 대기 후 반환
     */
    private NaverProperties.Client selectClient() {
        List<NaverProperties.Client> clients = naverProperties.getClients();
        if (clients == null || clients.isEmpty()) {
            throw new IllegalStateException("네이버 API 클라이언트 설정이 누락되었습니다.");
        }

        int size = clients.size();
        long now = System.currentTimeMillis();
        long minUntil = Long.MAX_VALUE;
        int fallbackIdx = -1;

        // 1. 즉시 사용 가능한 키 탐색
        for (int tries = 0; tries < size; tries++) {
            int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
            long until = cooldownUntil.getOrDefault(idx, 0L);

            if (now >= until) {
                lastKeyIndex.set(idx);
                return clients.get(idx);
            }

            if (until < minUntil) {
                minUntil = until;
                fallbackIdx = idx;
            }
        }

        // 2. 모든 키가 쿨다운 중일 경우: 가장 빨리 풀리는 키를 대기 후 사용
        if (fallbackIdx >= 0 && minUntil != Long.MAX_VALUE) {
            long sleepMs = Math.min(Math.max(minUntil - now, 50), 500);
            try { Thread.sleep(sleepMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            lastKeyIndex.set(fallbackIdx);
            return clients.get(fallbackIdx);
        }

        // 3. 최후의 수단: 단순히 다음 순번 키 반환
        int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), size);
        lastKeyIndex.set(idx);
        return clients.get(idx);
    }

    /**
     * 네이버 도서 검색 API 호출 (ISBN13 기준 단건 조회)
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

    /** ThreadLocal 데이터 정리 (스레드 풀 환경에서 메모리 누수 방지) */
    public void clearLastKeyIndex() {
        lastKeyIndex.remove();
    }
}