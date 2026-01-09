package com.booknara.booknaraPrj.mainpage.controller;

import com.booknara.booknaraPrj.mainpage.dto.LibrarianBookDTO;
import com.booknara.booknaraPrj.mainpage.service.LibrarianBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main/librarian")
public class LibrarianBookController {

    private final LibrarianBookService librarianBookService;

    @GetMapping("/books")
    public List<LibrarianBookDTO> getLibrarianBooks() {
        return librarianBookService.findLibrarianBooks();
    }
}

