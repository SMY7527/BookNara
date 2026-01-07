package com.booknara.booknaraPrj.security;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.security.oauth.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();
        User user;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            user = oAuth2User.getUser();
        } else if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else {
            response.sendRedirect("/home");
            return;
        }

        // 1) 추가정보 미완료면 무조건 여기
        if (!"Y".equals(user.getExtraInfoDone())) {
            response.sendRedirect("/users/signup-extra");
            return;
        }

        // 2) ✅ redirect 파라미터가 있으면 그쪽으로 (검색페이지 복귀용)
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            // 오픈 리다이렉트 방지: 내부 경로만 허용
            if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                response.sendRedirect(redirect);
                return;
            }
        }

        // 3) 기본은 기존대로 /home
        response.sendRedirect("/home");
    }
}
