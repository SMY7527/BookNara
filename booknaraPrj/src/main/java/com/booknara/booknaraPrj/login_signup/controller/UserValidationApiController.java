package com.booknara.booknaraPrj.login_signup.controller;


import com.booknara.booknaraPrj.login_signup.service.UserService1;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;



@RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/users")
    public class UserValidationApiController {

        private final UserService1 userService1;

        @GetMapping("/check-userid")
        public Map<String, Object> checkUserId(@RequestParam String userId) {
            boolean available = userService1.isUserIdAvailable(userId);

            return Map.of(
                    "available", available,
                    "message", available ? "사용 가능한 아이디." : "이미 사용 중인 아이디."
            );
        }

        @GetMapping("/check-profilename")
        public Map<String, Object> checkProfileName(@RequestParam String profileNm) {
            boolean available = userService1.isProfileNameAvailable(profileNm);

            return Map.of(
                    "available", available,
                    "message", available ? "사용 가능한 프로필명." : "이미 사용 중인 프로필명."
            );
        }

    @GetMapping("/check-email")
    public Map<String, Object> checkEmail(@RequestParam String email) {
        boolean available = userService1.isEmailAvailable(email);

        return Map.of(
                "available", available,
                "message", available
                        ? "사용 가능한 이메일이."
                        : "이미 가입된 이메일입니다."
        );
    }
    }

