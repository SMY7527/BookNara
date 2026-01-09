package com.booknara.booknaraPrj.login_signup.mapper;

import com.booknara.booknaraPrj.login_signup.dto.SocialAccount;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface SocialAccountMapper {
    SocialAccount findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    void insertSocialAccount(SocialAccount socialAccount);
}
