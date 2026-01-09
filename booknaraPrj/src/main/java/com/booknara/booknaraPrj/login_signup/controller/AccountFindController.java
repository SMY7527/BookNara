package com.booknara.booknaraPrj.login_signup.controller;

import com.booknara.booknaraPrj.login_signup.service.EmailService;
import com.booknara.booknaraPrj.login_signup.service.UserService1;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class AccountFindController {

    private final UserService1 userService1;
    private final EmailService emailService;

    // ✅ 화면
    @GetMapping("/find-account")
    public String findAccountPage() {
        return "find-account";
    }

    // =========================
    // 아이디 찾기
    // =========================
    @PostMapping("/find-id")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> req) {

        String userId = userService1.findUserId(
                req.get("name"),
                req.get("email")
        );

        if (userId == null) {
            return Map.of("success", false, "message", "일치하는 계정이 없어요");
        }

        return Map.of(
                "success", true,
                "message", "가입된 아이디는 " + userId + " 이에유"
        );
    }

    // =========================
    // 비밀번호 찾기 - 1단계
    // =========================
    @PostMapping("/find-password")
    @ResponseBody
    public Map<String, Object> findPassword(
            @RequestBody Map<String, String> req,
            HttpSession session) {

        boolean valid = userService1.checkUserForPasswordReset(
                req.get("userId"),
                req.get("email")
        );

        if (!valid) {
            return Map.of("success", false, "message", "정보가 맞질 않아요");
        }

        String code = userService1.createVerifyCode();

        session.setAttribute("PW_RESET_USER", req.get("userId"));
        session.setAttribute("PW_VERIFY_CODE", code);
        session.setAttribute("PW_VERIFY_TIME", System.currentTimeMillis());

        emailService.sendVerifyCode(req.get("email"), code);

        return Map.of("success", true, "message", "인증코드 보냈어유");
    }

    // =========================
    // 인증코드 검증
    // =========================

    @PostMapping("/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(
            @RequestBody Map<String, String> req,
            HttpSession session) {

        String savedCode = (String) session.getAttribute("PW_VERIFY_CODE");
        Long time = (Long) session.getAttribute("PW_VERIFY_TIME");

        if (savedCode == null || time == null) {
            return Map.of("success", false, "message", "인증 정보가 없어요");
        }

        if (System.currentTimeMillis() - time > 5 * 60 * 1000) {
            return Map.of("success", false, "message", "시간 초과됐어유");
        }

        if (!savedCode.equals(req.get("code"))) {
            return Map.of("success", false, "message", "인증코드 틀렸어유");
        }

        session.setAttribute("PW_VERIFIED", true);
        return Map.of("success", true, "message", "인증 완료");
    }

    // =========================
    // 비밀번호 재설정 화면
    // =========================
    @GetMapping("/reset-password-form")
    public String resetPasswordForm(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("PW_VERIFIED");
        if (verified == null || !verified) {
            return "redirect:/users/login";
        }

        return "reset-password"; // reset-password.html
    }

    // =========================
    // 비밀번호 재설정
    // =========================
    @PostMapping("/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(
            @RequestBody Map<String, String> req,
            HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("PW_VERIFIED");
        String userId = (String) session.getAttribute("PW_RESET_USER");

        if (verified == null || !verified || userId == null) {
            return Map.of("success", false, "message", "잘못된 접근이여");
        }

        userService1.resetPassword(userId, req.get("password"));

        // 🔐 보안상 세션 정리
        session.removeAttribute("PW_VERIFIED");
        session.removeAttribute("PW_RESET_USER");
        session.removeAttribute("PW_VERIFY_CODE");
        session.removeAttribute("PW_VERIFY_TIME");

        return Map.of("success", true, "message", "비밀번호 변경됐어유");
    }
}