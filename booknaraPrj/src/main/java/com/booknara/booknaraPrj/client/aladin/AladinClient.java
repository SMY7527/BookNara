package com.booknara.booknaraPrj.client.aladin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AladinClient {

    private final AladinProperties aladinProperties;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://www.aladin.co.kr/ttb/api")
            .build();

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    private String selectKey() {
        List<String> keys = aladinProperties.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("Aladin API keys not configured");
        }
        int idx = Math.floorMod(apiKeyIndex.getAndIncrement(), keys.size());
        return keys.get(idx);
    }

    // 알라딘 ItemLookUp API를 1회 호출해서 DTO로 반환 (재시도/키교체는 Service에서)
    public AladinResponse searchByIsbnOnce(String isbn13) {
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
                    .retrieve()
                    .body(AladinResponse.class);

        } catch (RestClientResponseException e) {
            throw e;
        }
    }
}
