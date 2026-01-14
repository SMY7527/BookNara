package com.booknara.booknaraPrj.bookcirculation.status.controller;

import com.booknara.booknaraPrj.bookcirculation.status.dto.BookCirculationStatusDTO;
import com.booknara.booknaraPrj.bookcirculation.status.service.BookCirculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [BookCirculationController]
 * 특정 도서의 대출 가능 여부, 예약 현황 및 로그인 사용자의 이용 상태를 조회하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/circulation")
public class BookCirculationController {

    private final BookCirculationService service;

    /**
     * [Spring Security 인증 객체에서 사용자 ID 추출]
     * @param auth 인증 객체
     * @return 로그인한 경우 userId(Username), 비로그인인 경우 null
     */
    private String getUserId(Authentication auth) {
        return (auth == null) ? null : auth.getName();
    }

    /**
     * [도서 순환 상태 종합 조회]
     * - URL: /book/circulation/status?isbn13=...
     * - 역할: 특정 도서(ISBN)에 대한 전체 재고 현황과 '나'의 대출/예약 관계를 한 번에 반환합니다.
     * - 활용: 도서 상세 페이지의 [대출하기], [예약하기], [연장하기] 버튼 활성화 로직의 기준 데이터가 됩니다.
     * * @param isbn13 조회할 도서의 ISBN
     * @param auth   현재 로그인한 사용자 정보 (비로그인 사용자도 조회가 가능해야 하므로 필수값은 아님)
     */
    @GetMapping("/status")
    public BookCirculationStatusDTO status(@RequestParam String isbn13, Authentication auth) {
        // 서비스 레이어에 ISBN과 (있다면) 사용자 ID를 전달하여 상태 객체를 생성
        return service.getStatus(isbn13, getUserId(auth));
    }
}