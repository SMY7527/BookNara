package com.booknara.booknaraPrj.feed.review.controller;

import com.booknara.booknaraPrj.feed.review.dto.ReviewListDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewPermissionDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSaveRequestDTO;
import com.booknara.booknaraPrj.feed.review.service.FeedReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * [FeedReviewController]
 * 도서 리뷰의 CRUD 및 권한 확인을 담당하는 REST 컨트롤러입니다.
 * 모든 응답은 JSON 형식으로 제공되어 프론트엔드(AJAX)와의 통신에 최적화되어 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/review")
public class FeedReviewController {

    private final FeedReviewService service;

    /**
     * [인증 정보 추출 유틸리티]
     * Spring Security의 Authentication 객체에서 안전하게 사용자 ID를 추출합니다.
     * 비로그인 상태("anonymousUser")인 경우 null을 반환하여 서비스 레이어에서 처리하도록 돕습니다.
     */
    private String userId(Authentication auth) {
        if (auth == null) return null;
        String name = auth.getName();
        if (name == null || "anonymousUser".equalsIgnoreCase(name)) return null;
        return name;
    }

    /**
     * [리뷰 작성 권한 확인]
     * GET /book/review/permission?isbn13=...
     * 사용자의 대출/반납 이력을 기반으로 리뷰 작성 가능 여부를 반환합니다.
     */
    @GetMapping("/permission")
    public ReviewPermissionDTO permission(@RequestParam String isbn13, Authentication auth) {
        return service.getReviewPermission(isbn13, userId(auth));
    }

    /**
     * [리뷰 저장/수정]
     * POST /book/review/save
     * 단일 엔드포인트에서 신규 등록과 기존 리뷰 수정을 동시에 처리합니다.
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody ReviewSaveRequestDTO req, Authentication auth) {
        try {
            String feedId = service.saveReview(req, userId(auth));
            return Map.of("ok", true, "feedId", feedId);
        } catch (IllegalArgumentException e) {
            // 잘못된 파라미터 요청 (예: 평점 범위 초과)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            // 권한 없음 (예: 반납 이력 없음, 남의 글 수정 시도)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * [리뷰 목록 조회]
     * GET /book/review/list?isbn13=...&page=1&size=5
     * 페이징 처리된 리뷰 목록과 통계 정보를 반환하며, 로그인 시 '내 글 여부'를 함께 표시합니다.
     */
    @GetMapping("/list")
    public ReviewListDTO list(@RequestParam String isbn13,
                              @RequestParam(defaultValue="1") int page,
                              @RequestParam(defaultValue="5") int size,
                              Authentication auth) {
        // userId를 넘겨줌으로써 각 리뷰 항목에 mineYn(본인 글 여부) 플래그를 세팅합니다.
        return service.getPage(isbn13, page, size, userId(auth));
    }

    /**
     * [리뷰 삭제]
     * DELETE /book/review/{feedId}
     * 본인이 작성한 리뷰만 삭제할 수 있도록 검증 로직이 포함되어 있습니다.
     */
    @DeleteMapping("/{feedId}")
    public Map<String, Object> delete(@PathVariable String feedId, Authentication auth) {
        try {
            service.deleteMyReview(feedId, userId(auth));
            return Map.of("ok", true);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}