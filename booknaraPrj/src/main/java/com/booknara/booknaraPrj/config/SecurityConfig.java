package com.booknara.booknaraPrj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/test/**").permitAll()   //  테스트만 오픈
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());         // 기본 로그인 유지

        return http.build();
    }
}
