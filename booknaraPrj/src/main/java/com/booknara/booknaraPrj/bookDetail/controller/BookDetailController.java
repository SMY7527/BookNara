package com.booknara.booknaraPrj.bookDetail.controller;

import com.booknara.booknaraPrj.bookDetail.dto.BookDetailViewDTO;
import com.booknara.booknaraPrj.bookDetail.service.BookDetailService;
import com.booknara.booknaraPrj.bookMark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
    private final BookmarkService bookmarkService;


    @GetMapping("/book/detail/{isbn13}")
    public String bookDetail(@PathVariable String isbn13, Authentication auth, Model model) {

        String userId = (auth != null && !(auth instanceof AnonymousAuthenticationToken))
                ? auth.getName()
                : null;

        BookDetailViewDTO view = bookDetailService.getBookDetailView(isbn13, userId);
        if (view == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn13);
        }

        // ✅ 초기 북마크 상태/카운트
        String bookmarkedYn = "N";
        if (userId != null && !userId.isBlank()) {
            bookmarkedYn = bookmarkService.isBookmarked(isbn13, userId) ? "Y" : "N";
        }
        int bookmarkCnt = bookmarkService.countByIsbn(isbn13);

        // ✅ view에 주입 (HTML이 view.* 로 접근하므로 필수)
        view.setBookmarkedYn(bookmarkedYn);
        view.setBookmarkCnt(bookmarkCnt);

        model.addAttribute("view", view);
        return "book/bookDetail";
    }
}
