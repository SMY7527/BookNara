package com.booknara.booknaraPrj.client.naver;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NaverClient {
    private final RestClient restClient;

    public NaverClient() {
        this.restClient=RestClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }
}
/v1/search/book_adv.xml