package com.booknara.booknaraPrj.mainpage.controller;
import com.booknara.booknaraPrj.mainpage.dto.HashtagDTO;
import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import com.booknara.booknaraPrj.mainpage.service.MallangPickService;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main/mallang-pick")
public class MallangPickController {

    private final MallangPickService mallangPickService;

    // ⭐ 해시태그 3개
    @GetMapping("/hashtags")
    public List<HashtagDTO> hashtags(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String userId = (user == null) ? null : user.getUserId();
        return mallangPickService.pickHashtags(userId);
    }

    // ⭐ 도서 3권
    @GetMapping("/books")
    public List<MallangPickDTO> books(@RequestParam int genreId) {
        return mallangPickService.pickBooks(genreId);
    }
}
