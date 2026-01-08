package com.booknara.booknaraPrj.bookDetail.controller;

import com.booknara.booknaraPrj.bookDetail.dto.BookDetailViewDTO;
import com.booknara.booknaraPrj.bookDetail.service.BookDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private final BookDetailService bookDetailService;

    @GetMapping("/book/detail/{isbn13}")
    public String bookDetail(@PathVariable String isbn13, Authentication auth, Model model) {

        String userId = (auth != null) ? auth.getName() : null;

        BookDetailViewDTO view = bookDetailService.getBookDetailView(isbn13, userId);
        if (view == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn13);
        }

        model.addAttribute("view", view);
        return "book/bookDetail";
    }
}
