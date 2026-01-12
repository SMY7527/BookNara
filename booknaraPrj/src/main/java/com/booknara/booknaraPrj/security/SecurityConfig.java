package com.booknara.booknaraPrj.security;

import com.booknara.booknaraPrj.security.oauth.CustomOAuth2SuccessHandler;
import com.booknara.booknaraPrj.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        RequestMatcher apiMatcher = request -> request.getRequestURI().startsWith("/api/");

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/book/review/list",
                                "/book/cart/*/status"
                        ).permitAll()

                        .requestMatchers(
                                "/home",
                                "/users/login",
                                "/users/signup",
                                "/users/find-account",
                                "/users/signup-extra",
                                "/users/check-id",
                                "/users/check-profile",
                                "/users/social/**",

                                "/book/search",
                                "/book/search/list",
                                "/book/detail/**",
                                "/book/genres/**",

                                "/book/reviewstatus/**",
                                "/book/circulation/status",

                                "/api/bookmarks/**",
                                "/api/users/**",

                                "/oauth2/**",
                                "/login/oauth2/**",

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
                        .requestMatchers("/api/reports/**").authenticated()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                                .defaultAuthenticationEntryPointFor(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                        apiMatcher
                                )
                )

                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
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

