package com.booknara.booknaraPrj.bookcirculation.controller;

import com.booknara.booknaraPrj.bookcirculation.dto.BookCirculationStatusDTO;
import com.booknara.booknaraPrj.bookcirculation.service.BookCirculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/circulation")
public class BookCirculationController {

    private final BookCirculationService service;

    private String getUserId(Authentication auth) {
        return (auth == null) ? null : auth.getName();
    }

    @GetMapping("/status")
    public BookCirculationStatusDTO status(@RequestParam String isbn13, Authentication auth) {
        return service.getStatus(isbn13, getUserId(auth));
    }
}
