package com.booknara.booknaraPrj.sample.controller;

import com.booknara.booknaraPrj.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.client.aladin.AladinClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AladinTestController {

    private final AladinClient aladinClient;

    @GetMapping("/test/aladin/{isbn13}")
    public AladinDTO test(@PathVariable String isbn13) {

        AladinResponse response = aladinClient.searchByIsbnOnce(isbn13);

        if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
            return null;
        }

        // 알라딘은 item[0]만 사용
        return response.getItem().get(0);
    }
}
