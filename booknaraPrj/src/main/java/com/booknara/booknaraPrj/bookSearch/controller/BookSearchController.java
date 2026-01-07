package com.booknara.booknaraPrj.bookSearch.controller;

import com.booknara.booknaraPrj.bookSearch.dto.BookSearchConditionDTO;
import com.booknara.booknaraPrj.bookSearch.dto.BookSearchDTO;
import com.booknara.booknaraPrj.bookSearch.dto.PageInsertDTO;
import com.booknara.booknaraPrj.bookSearch.service.BookSearchService;
import com.booknara.booknaraPrj.bookSearch.dto.PageResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/book")
public class BookSearchController {

    private final BookSearchService service;

    /**
     * 도서 검색 페이지(화면)
     * - 최초 진입 시 HTML만 반환
     */
    @GetMapping("/search")
    public String searchPage() {
        return "book/booksearch"; // templates/book/booksearch.html
    }

    /**
     * 도서 검색 API(JSON)
     *  fetch/Ajax로 호출
     */
    @GetMapping("/search/list")
    @ResponseBody
    public PageResultDTO<BookSearchDTO> searchApi(BookSearchConditionDTO cond, PageInsertDTO page) {

        System.out.println("[COND] mall=" + cond.getMall()
                + ", parentGenreId=" + cond.getParentGenreId()
                + ", genreId=" + cond.getGenreId()
                + ", ebookYn=" + cond.getEbookYn()
                + ", keyword=" + cond.getKeyword()
                + ", field=" + cond.getField());

        System.out.println("[PAGE] page=" + page.getPage() + ", size=" + page.getSize());

        return service.search(cond, page);
    }
}
