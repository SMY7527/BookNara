package com.booknara.booknaraPrj.login_signup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialLinkService {

    private final SocialAccountMapper socialAccountMapper;

    @Transactional
    public void link(String userId, String provider, String providerId) {

        // 1) 재검증 (이중클릭/새로고침/경합 방지)
        SocialAccount already = socialAccountMapper.findByProviderAndProviderId(provider, providerId);
        if (already != null) {
            throw new IllegalStateException("이미 다른 계정에 연동된 소셜 계정이여유.");
        }

        // 2) INSERT
        SocialAccount sa = new SocialAccount();
        sa.setUserId(userId);
        sa.setProvider(provider);
        sa.setProviderId(providerId);

        try {
            socialAccountMapper.insertSocialAccount(sa);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // DB UNIQUE(provider, provider_id) 경합 방어
            throw new IllegalStateException("이미 연동 처리된 소셜 계정이여유.");
        }
    }
}

