package com.booknara.booknaraPrj.login_signup.controller;


import com.booknara.booknaraPrj.login_signup.mapper.UserMallangMapper;
import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class HeaderAdvice {

    private final UserMallangMapper userMallangMapper;

    @ModelAttribute("profileImage")
    public String profileImage(Authentication authentication) {

        // 로그인 안 했을 때
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return null;
        }

        CustomUserDetails user = (CustomUserDetails) principal;

        // USE_IMG 기준으로 말랑이 or 프로필 이미지 반환
        return userMallangMapper.selectProfileImage(user.getUserId());
    }
}

