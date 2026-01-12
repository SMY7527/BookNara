package com.booknara.booknaraPrj.security.oauth;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.dto.SocialAccount;
import com.booknara.booknaraPrj.login_signup.mapper.SocialAccountMapper;
import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialAccountMapper socialAccountMapper;
    private final UserMapper userMapper;
    private final UserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        String provider = principal.getProvider();     // "KAKAO"
        String providerId = principal.getProviderId(); // 카카오 id
        String email = principal.getEmail();

        // 1️⃣ provider + providerId 최우선 조회
        SocialAccount social = socialAccountMapper.findByProviderAndProviderId(provider, providerId);

        if (social != null) {
            // 이미 연동된 소셜: social.userId로 강제 로그인
            forceLogin(social.getUserId(), request);

            User linkedUser = userMapper.findByUserId(social.getUserId());
            if (linkedUser != null && !"Y".equals(linkedUser.getExtraInfoDone())) {
                response.sendRedirect("/users/signup-extra");
            } else {
                response.sendRedirect("/home");
            }
            return;
        }

        // 2️⃣ 이메일 기준 기존 USER 조회
        User existingUser = userMapper.findByEmail(email);

        // ⭐ 취소 플래그가 Y면 연동확인으로 보내지 말고 신규 생성 루트로 넘어가야 함
        HttpSession curSession = request.getSession(false);
        boolean forceNew = curSession != null
                && "Y".equals(curSession.getAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER));

        if (!forceNew && existingUser != null) {
            // ✅ 여기서 세션/인증을 확실하게 끊고 link-confirm으로 보냄

            SecurityContextHolder.clearContext();

            HttpSession old = request.getSession(false);
            if (old != null) {
                old.invalidate(); // ⭐ 인증 찌꺼기 싹 제거
            }

            HttpSession session = request.getSession(true);

            session.setAttribute(SocialLinkSessionKey.LINK_PROVIDER, provider);
            session.setAttribute(SocialLinkSessionKey.LINK_PROVIDER_ID, providerId);
            session.setAttribute(SocialLinkSessionKey.LINK_EMAIL, email);
            session.setAttribute(SocialLinkSessionKey.LINK_USER_ID, existingUser.getUserId());

            // 취소 전까진 강제 신규 생성 아님
            session.setAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER, "N");

            response.sendRedirect("/users/social/link-confirm");
            return;
        }

        // 3️⃣ 신규 소셜 (또는 forceNew=Y로 인해 신규 생성이 진행될 케이스)
        // 주의: 실제 신규 생성은 CustomOAuth2UserService에서 수행됨.
        User user = principal.getUser();

        if (!"Y".equals(user.getExtraInfoDone())) {
            response.sendRedirect("/users/signup-extra");
        } else {
            response.sendRedirect("/home");
        }
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
