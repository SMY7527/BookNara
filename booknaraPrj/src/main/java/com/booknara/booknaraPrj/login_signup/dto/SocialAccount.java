package com.booknara.booknaraPrj.login_signup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SocialAccount {

    /** 소셜 계정 PK (UUID) */
    private String socialId;

    /** USERS.USER_ID (FK) */
    private String userId;

    /** 소셜 제공자 (KAKAO, NAVER, GOOGLE) */
    private String provider;

    /** 소셜 고유 ID */
    private String providerId;

    /** 연동 생성일 */
    private LocalDateTime createdAt;
}