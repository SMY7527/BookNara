package com.booknara.booknaraPrj.mainpage.controller;

import com.booknara.booknaraPrj.mainpage.dto.NewBookDTO;
import com.booknara.booknaraPrj.mainpage.service.NewBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main/new-books")
public class NewBookController {

    private final NewBookService newBookService;

    @GetMapping
    public List<NewBookDTO> getNewBooks() {
        return newBookService.findLatestBooks();
    }
}

