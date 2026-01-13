package com.booknara.booknaraPrj.mypage.info;

import com.booknara.booknaraPrj.login_signup.service.UserService1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage/password")
public class MyPagePasswordController {

    private final UserService1 userService;

    /**
     * 1️⃣ 현재 비밀번호 검증
     * POST /mypage/password/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCurrentPassword(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String currentPassword = body.get("password");

        if (currentPassword == null || currentPassword.isBlank()) {
            return ResponseEntity.badRequest().body("비밀번호를 입력해 주세요.");
        }

        // 로그인 사용자 ID
        String userId = authentication.getName();

        try {
            userService.verifyCurrentPassword(userId, currentPassword);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * 2️⃣ 비밀번호 변경
     * POST /mypage/password/change
     */
    @PostMapping("/change")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("새 비밀번호를 입력해 주세요.");
        }

        String userId = authentication.getName();

        try {
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}

