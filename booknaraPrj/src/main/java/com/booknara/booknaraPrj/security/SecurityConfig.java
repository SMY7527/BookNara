package com.booknara.booknaraPrj.security;

import com.booknara.booknaraPrj.security.oauth.CustomOAuth2SuccessHandler;
import com.booknara.booknaraPrj.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {



        http
                .csrf(csrf -> csrf.disable())



                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/home",                 // 메인(비로그인 허용이면)
                                "/users/login",
                                "/users/signup",
                                "/users/find-account",
                                "/users/signup-extra",
                                "/users/check-id",
                                "/users/check-profile",
                                "/users/social/**",
                                "/book/search/**",
                                "/book/search/list",
                                "/api/bookmarks/**",
                                "/book/detail/**",
                                "/book/genres/**",
                                "/book/reviewstatus/**",
                                "/book/circulation/status",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/users/**",
                                "/users/reset-password-form",
                                "/users/reset-password",
                                "/recommend/**",
                                "/api/recommend/**",
                                "/api/**",
                                "/api/main/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error/*",
                                "/404"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/users/find-id",
                                "/users/find-password",
                                "/users/verify-code",
                                "/users/reset-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/users/login")        // ⭐ 로그인 페이지
                        .loginProcessingUrl("/users/login") // ⭐ POST 로그인 처리
                        .usernameParameter("userId")     // ⭐ USER_ID
                        .passwordParameter("password")   // ⭐ PASSWORD
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/users/login?error")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/users/login")
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                )





                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

