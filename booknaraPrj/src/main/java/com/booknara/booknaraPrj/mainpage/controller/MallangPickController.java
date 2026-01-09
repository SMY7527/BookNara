package com.booknara.booknaraPrj.mainpage.controller;

import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import com.booknara.booknaraPrj.mainpage.service.MallangPickService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main/mallang-pick")
public class MallangPickController {

    private final MallangPickService mallangPickService;

    @GetMapping("/books")
    public List<MallangPickDTO> getMallangPickBooks(@RequestParam int genreId) {
        return mallangPickService.findMallangPickBooks(genreId);
    }
}

