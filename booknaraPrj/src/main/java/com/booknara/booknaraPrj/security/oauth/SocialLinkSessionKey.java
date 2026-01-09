package com.booknara.booknaraPrj.security.oauth;

public final class SocialLinkSessionKey {
    public static final String LINK_PROVIDER = "LINK_PROVIDER";
    public static final String LINK_PROVIDER_ID = "LINK_PROVIDER_ID";
    public static final String LINK_EMAIL = "LINK_EMAIL";
    public static final String LINK_USER_ID = "LINK_USER_ID";

    // ⭐ 취소 시: 이메일이 이미 있어도 새 계정 강제 생성
    public static final String FORCE_NEW_SOCIAL_USER = "FORCE_NEW_SOCIAL_USER";

    private SocialLinkSessionKey() {}
}