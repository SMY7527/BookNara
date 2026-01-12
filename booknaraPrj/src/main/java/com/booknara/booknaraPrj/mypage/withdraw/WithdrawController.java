package com.booknara.booknaraPrj.mypage.withdraw;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class WithdrawController {

    private final WithdrawService withdrawService;

    // ✅ 캡차 이미지 생성 (세션에 정답 저장)
    @GetMapping(value = "/withdraw/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] captcha(HttpSession session) {
        return withdrawService.generateCaptchaPng(session);
    }

    // ✅ 회원탈퇴 처리(삭제 X, 상태만 변경)
    @PostMapping("/withdraw")
    @ResponseBody
    public ResponseEntity<String> withdraw(
            @RequestParam String captcha,
            @RequestParam String password,
            HttpSession session
    ) {
        // 지금은 고정 유저
        String userId = "user01";

        boolean ok = withdrawService.withdraw(userId, password, captcha, session);

        if (ok) return ResponseEntity.ok("OK");
        return ResponseEntity.badRequest().body("보안문자 또는 비밀번호가 올바르지 않습니다.");
    }
}
