package com.booknara.booknaraPrj.bookSearch.controller;

import com.booknara.booknaraPrj.bookSearch.dto.BookSearchConditionDTO;
import com.booknara.booknaraPrj.bookSearch.dto.BookSearchDTO;
import com.booknara.booknaraPrj.bookSearch.dto.PageInsertDTO;
import com.booknara.booknaraPrj.bookSearch.service.BookSearchService;
import com.booknara.booknaraPrj.bookSearch.dto.PageResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * [BookSearchController]
 * 도서 검색 화면 랜더링 및 검색 API 요청을 처리하는 컨트롤러입니다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/book")
public class BookSearchController {

    private final BookSearchService service;

    /**
     * [도서 검색 메인 페이지 호출]
     * - URL: /book/search
     * - 역할: 검색어(keyword)를 파라미터로 받아 초기 검색 화면(HTML)을 반환합니다.
     * - 특징: SSR(Server Side Rendering) 방식으로 초기 키워드만 전달하고 상세 데이터는 API를 통해 로드합니다.
     */
    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String keyword, Model model) {
        // 사용자가 메인 페이지 등에서 검색어를 입력하고 넘어왔을 때, 해당 검색어를 입력창에 유지하기 위해 전달
        model.addAttribute("keyword", keyword);
        return "book/booksearch"; // src/main/resources/templates/book/booksearch.html 호출
    }


    /**
     * [실시간 도서 검색 API]
     * - URL: /book/search/list
     * - 역할: 화면의 '검색', '필터 변경', '페이지 번호 클릭' 시 Ajax로 호출되어 검색 결과를 반환합니다.
     * - 반환 포맷: @ResponseBody를 통해 PageResultDTO 객체를 JSON 형태로 반환합니다.
     */
    @GetMapping("/search/list")
    @ResponseBody
    public PageResultDTO<BookSearchDTO> searchApi(BookSearchConditionDTO cond, PageInsertDTO page) {

        // 운영 및 디버깅용 로그: 유입된 검색 조건과 페이징 정보를 콘솔에 출력
        System.out.println("[검색 요청 인입] 몰=" + cond.getMall()
                + ", 부모장르=" + cond.getParentGenreId()
                + ", 상세장르=" + cond.getGenreId()
                + ", 이북여부=" + cond.getEbookYn()
                + ", 키워드=" + cond.getKeyword()
                + ", 검색필드=" + cond.getField());

        System.out.println("[페이징 요청 인입] 페이지=" + page.getPage() + ", 사이즈=" + page.getSize());

        // 비즈니스 서비스 호출: 정규화 및 검색 수행 후 결과를 반환
        return service.search(cond, page);
    }

}