package com.booknara.booknaraPrj.security.oauth;

import com.booknara.booknaraPrj.login_signup.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final String provider;
    private final String providerId;
    private final String email;

    public CustomOAuth2User(User user, String provider,
                            String providerId,String email,
                            Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }

    public User getUser() {
        return user;
    }
    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEmail() {
        return email;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (user.getUserRole() == 0) ? "ADMIN" : "USER";

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    // ⭐ 화면에서 쓰기 좋은 값들
    public String getProfileNm() {
        return user.getProfileNm();
    }

    public String getUserId() {
        return user.getUserId();
    }


}
