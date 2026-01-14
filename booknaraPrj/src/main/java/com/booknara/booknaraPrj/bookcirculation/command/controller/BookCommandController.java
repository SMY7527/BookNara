package com.booknara.booknaraPrj.bookcirculation.command.controller;

import com.booknara.booknaraPrj.bookcirculation.command.dto.*;
import com.booknara.booknaraPrj.bookcirculation.command.service.BookCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * [BookCommandController]
 * 도서 대출, 연장, 예약, 반납 등 '상태 변경'을 수반하는 모든 요청을 처리하는 컨트롤러입니다.
 * 모든 API는 로그인된 사용자만 접근할 수 있도록 보안 처리가 되어 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/circulation")
public class BookCommandController {

    private final BookCommandService service;

    /**
     * [사용자 인증 및 ID 추출 공통 로직]
     * - Spring Security의 Authentication 객체를 분석하여 로그인 여부를 확인합니다.
     * - 비로그인 또는 익명 사용자의 경우 401(Unauthorized) 에러를 즉시 반환하여 보안을 강화합니다.
     */
    private String userId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다.");
        }
        return auth.getName();
    }

    /** [도서 대출 신청] 특정 도서(ISBN)에 대해 대출 트랜잭션을 실행합니다. */
    @PostMapping("/lend")
    public LendResultDTO lend(@RequestParam String isbn13, Authentication auth) {
        return service.lend(isbn13, userId(auth));
    }

    /** [대출 연장 신청] 현재 대출 중인 도서의 반납 예정일을 연장합니다. */
    @PostMapping("/extend")
    public ExtendResultDTO extend(@RequestParam String lendId, Authentication auth) {
        return service.extend(lendId, userId(auth));
    }

    /** [도서 예약 신청] 대출 불가능한 도서에 대해 예약 대기를 등록합니다. */
    @PostMapping("/reserve")
    public ReserveResultDTO reserve(@RequestParam String isbn13, Authentication auth) {
        return service.reserve(isbn13, userId(auth));
    }

    /** [예약 취소] 등록했던 도서 예약 대기를 철회합니다. */
    @PostMapping("/reserve/cancel")
    public CancelReserveResultDTO cancelReserve(@RequestParam String rsvId, Authentication auth) {
        return service.cancelReserve(rsvId, userId(auth));
    }

    /** [무인 반납함 투입] 실물 도서를 반납함에 넣었음을 시스템에 1차 등록합니다. */
    @PostMapping("/return/box")
    public ReturnBoxResultDTO returnToBox(@RequestParam String lendId,
                                          @RequestParam Long boxId,
                                          Authentication auth) {
        return service.returnToBox(lendId, boxId, userId(auth));
    }

    /** [최종 반납 확정] 사서 확인 등을 거쳐 대출 기록을 완전히 종결합니다. */
    @PostMapping("/return/confirm")
    public ReturnConfirmResultDTO confirmReturn(@RequestParam String lendId, Authentication auth) {
        return service.confirmReturn(lendId, userId(auth));
    }
}