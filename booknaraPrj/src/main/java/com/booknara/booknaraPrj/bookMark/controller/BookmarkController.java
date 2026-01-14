package com.booknara.booknaraPrj.bookMark.controller;

import com.booknara.booknaraPrj.bookMark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * [BookmarkController]
 * 사용자의 관심 도서(북마크) 상태를 조회하고 전환(Toggle)하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * [북마크 상태 조회]
     * - 특정 도서(ISBN)에 대해 현재 로그인한 사용자가 북마크를 했는지 확인합니다.
     * - 주로 도서 상세 페이지 진입 시 초기 아이콘 상태를 결정하기 위해 호출됩니다.
     */
    @GetMapping("/{isbn13}")
    public ResponseEntity<?> status(@PathVariable String isbn13, Authentication auth) {
        // 비로그인 사용자의 경우 401 에러와 함께 메시지 반환
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "UNAUTHORIZED"));
        }

        String userId = auth.getName();
        boolean bookmarked = bookmarkService.isBookmarked(isbn13, userId);

        return ResponseEntity.ok(Map.of("isbn13", isbn13, "bookmarked", bookmarked));
    }

    /**
     * [북마크 토글]
     * - 사용자가 북마크 아이콘을 클릭했을 때 호출됩니다.
     * - 기존에 북마크가 있으면 삭제, 없으면 추가하는 '원클릭' 방식입니다.
     */
    @PostMapping("/{isbn13}/toggle")
    public ResponseEntity<?> toggle(@PathVariable String isbn13, Authentication auth) {
        // 익명 사용자(비로그인) 접근 차단
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).body(Map.of("message", "UNAUTHORIZED"));
        }

        String userId = auth.getName();

        // 1) 상태 반전(Add or Delete) 수행
        boolean bookmarked = bookmarkService.toggle(isbn13, userId);
        // 2) 해당 도서의 총 북마크 수 집계 (인기 지표)
        int cnt = bookmarkService.countByIsbn(isbn13);

        // 프론트엔드의 다양한 상황을 배려한 풍부한 응답 객체 반환
        return ResponseEntity.ok(Map.of(
                "isbn13", isbn13,
                "bookmarked", bookmarked,                 // ✅ 리스트 페이지(Boolean) 대응
                "bookmarkedYn", bookmarked ? "Y" : "N",   // ✅ 상세 페이지(Y/N) 대응
                "bookmarkCnt", cnt                        // ✅ 상세 페이지 실시간 카운트 업데이트용
        ));
    }

}