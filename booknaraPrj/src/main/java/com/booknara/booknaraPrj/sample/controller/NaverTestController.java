package com.booknara.booknaraPrj.sample.controller;

import com.booknara.booknaraPrj.client.naver.NaverDTO;
import com.booknara.booknaraPrj.service.sync.naver.NaverErrResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NaverTestController {

    private final NaverErrResponseService naverService;

    @GetMapping("/test/naver/{isbn13}")
    public NaverDTO test(@PathVariable String isbn13) {
        return naverService.searchByIsbnWithPolicy(isbn13);
    }
}
