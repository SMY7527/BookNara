package com.booknara.booknaraPrj.login_signup.controller;

import com.booknara.booknaraPrj.login_signup.service.SocialLinkService;
import com.booknara.booknaraPrj.security.oauth.SocialLinkSessionKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users/social")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;
    private final UserDetailsService userDetailsService;

    // 1️⃣ 연동 확인 화면
    @GetMapping("/link-confirm")
    public String linkConfirm(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/users/login";

        String email = (String) session.getAttribute(SocialLinkSessionKey.LINK_EMAIL);
        if (email == null) return "redirect:/users/login";

        model.addAttribute("email", email);
        return "users/social-link-confirm";
    }

    // 2️⃣ 연동 처리
    @PostMapping("/link")
    public String link(HttpServletRequest request, RedirectAttributes ra) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            ra.addFlashAttribute("errorMessage", "세션이 만료됐어유. 다시 로그인해줘유.");
            return "redirect:/users/login";
        }

        String provider = (String) session.getAttribute(SocialLinkSessionKey.LINK_PROVIDER);
        String providerId = (String) session.getAttribute(SocialLinkSessionKey.LINK_PROVIDER_ID);
        String userId = (String) session.getAttribute(SocialLinkSessionKey.LINK_USER_ID);

        if (provider == null || providerId == null || userId == null) {
            ra.addFlashAttribute("errorMessage", "연동 정보가 만료됐어유. 다시 시도해줘유.");
            return "redirect:/users/login";
        }

        try {
            socialLinkService.link(userId, provider, providerId);

            // ⭐ 로그인 주체 교체
            forceLogin(userId, request);

            // ⭐ 임시 세션 정리
            clearLinkSession(session);

            return "redirect:/home";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/social/link-confirm";
        }
    }

    // 3️⃣ 연동 취소 → "새 계정 생성" 강제 플래그 켜고, 소셜 인증 다시 태움
    @PostMapping("/link-cancel")
    public String cancel(HttpServletRequest request, RedirectAttributes ra) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            ra.addFlashAttribute("errorMessage", "세션이 만료됐어유. 다시 로그인해줘유.");
            return "redirect:/users/login";
        }

        String provider = (String) session.getAttribute(SocialLinkSessionKey.LINK_PROVIDER);
        if (provider == null) {
            ra.addFlashAttribute("errorMessage", "소셜 제공자 정보가 없슈. 다시 시도해줘유.");
            return "redirect:/users/login";
        }

        // ✅ 핵심: 취소 = 신규 생성 강제
        session.setAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER, "Y");

        // ✅ 연동 대상 로컬 userId는 제거 (연동을 안 할 거니까)
        session.removeAttribute(SocialLinkSessionKey.LINK_USER_ID);

        // ✅ 혹시 남아있을 인증 찌꺼기 제거
        SecurityContextHolder.clearContext();
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        // registrationId는 보통 소문자(kakao/google/naver)
        String registrationId = provider.toLowerCase();

        return "redirect:/oauth2/authorization/" + registrationId;
    }

    // 🔹 공통 메서드들
    private void clearLinkSession(HttpSession session) {
        session.removeAttribute(SocialLinkSessionKey.LINK_PROVIDER);
        session.removeAttribute(SocialLinkSessionKey.LINK_PROVIDER_ID);
        session.removeAttribute(SocialLinkSessionKey.LINK_EMAIL);
        session.removeAttribute(SocialLinkSessionKey.LINK_USER_ID);
        session.removeAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER);
    }

    private void forceLogin(String userId, HttpServletRequest request) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
