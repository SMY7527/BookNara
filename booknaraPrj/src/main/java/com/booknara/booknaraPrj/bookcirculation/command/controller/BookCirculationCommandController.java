package com.booknara.booknaraPrj.bookcirculation.command.controller;

import com.booknara.booknaraPrj.bookcirculation.command.dto.*;
import com.booknara.booknaraPrj.bookcirculation.command.service.BookCirculationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/circulation")
public class BookCirculationCommandController {

    private final BookCirculationCommandService service;

    private String userId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return auth.getName();
    }

    @PostMapping("/lend")
    public LendResultDTO lend(@RequestParam String isbn13, Authentication auth) {
        return service.lend(isbn13, userId(auth));
    }

    @PostMapping("/extend")
    public ExtendResultDTO extend(@RequestParam String lendId, Authentication auth) {
        return service.extend(lendId, userId(auth));
    }

    @PostMapping("/reserve")
    public ReserveResultDTO reserve(@RequestParam String isbn13, Authentication auth) {
        return service.reserve(isbn13, userId(auth));
    }

    @PostMapping("/reserve/cancel")
    public CancelReserveResultDTO cancelReserve(@RequestParam String rsvId, Authentication auth) {
        return service.cancelReserve(rsvId, userId(auth));
    }

    @PostMapping("/return/box")
    public ReturnBoxResultDTO returnToBox(@RequestParam String lendId,
                                          @RequestParam Long boxId,
                                          Authentication auth) {
        return service.returnToBox(lendId, boxId, userId(auth));
    }

    @PostMapping("/return/confirm")
    public ReturnConfirmResultDTO confirmReturn(@RequestParam String lendId, Authentication auth) {
        return service.confirmReturn(lendId, userId(auth));
    }
}
