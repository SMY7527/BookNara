package com.booknara.booknaraPrj.security.uitil;

import com.booknara.booknaraPrj.security.CustomUserDetails;
import com.booknara.booknaraPrj.security.oauth.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUserUtils {

    public static String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }

        if (principal instanceof CustomOAuth2User cou) {
            return cou.getUserId();
        }

        return null;
    }
}
