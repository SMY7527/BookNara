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

/**
 * [BookDetailController]
 * 도서 상세 페이지로의 이동과 해당 페이지에 필요한 모든 통합 데이터를 관리하는 컨트롤러입니다.
 */
@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private final BookDetailService bookDetailService;
    private final BookmarkService bookmarkService;

    /**
     * [도서 상세 페이지 호출]
     * - URL: /book/detail/{isbn13}
     * - 역할: ISBN을 경로 변수로 받아 해당 도서의 모든 정보(메타, 재고, 리뷰, 북마크)를 모델에 담아 전송합니다.
     */
    @GetMapping("/book/detail/{isbn13}")
    public String bookDetail(@PathVariable String isbn13, Authentication auth, Model model) {

        // 1. [보안] Spring Security 인증 객체로부터 로그인한 사용자 ID 추출
        // 익명 사용자(비로그인)인 경우 null로 처리하여 비로그인 접근을 허용합니다.
        String userId = (auth != null && !(auth instanceof AnonymousAuthenticationToken))
                ? auth.getName()
                : null;

        // 2. [데이터 집계] 도서 상세 정보 및 관련 부가 정보(재고, 장르 경로, 리뷰 등) 통합 조회
        BookDetailViewDTO view = bookDetailService.getBookDetailView(isbn13, userId);

        // 3. [예외 처리] 존재하지 않는 도서 요청 시 404 Not Found 에러 발생
        if (view == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "도서 정보를 찾을 수 없습니다: " + isbn13);
        }

        // 4. [개인화 데이터 주입] 북마크 상태 및 총 카운트 별도 계산
        // 서비스에서 처리할 수도 있지만, 컨트롤러 레이어에서 최종적으로 UI에 필요한 플래그를 세밀하게 조정합니다.
        String bookmarkedYn = "N";
        if (userId != null && !userId.isBlank()) {
            bookmarkedYn = bookmarkService.isBookmarked(isbn13, userId) ? "Y" : "N";
        }
        int bookmarkCnt = bookmarkService.countByIsbn(isbn13);

        // 5. [View 모델 완성] HTML(Thymeleaf)에서 'view.필드명'으로 접근할 수 있도록 최종 세팅
        view.setBookmarkedYn(bookmarkedYn);
        view.setBookmarkCnt(bookmarkCnt);

        model.addAttribute("view", view);

        // 6. [화면 반환] book/bookDetail.html 템플릿 렌더링
        return "book/bookDetail";
    }
}