package com.booknara.booknaraPrj.security.oauth;

import com.booknara.booknaraPrj.login_signup.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountMapper socialAccountMapper;
    private final UserMapper userMapper;

    private static final String SOCIAL_PASSWORD = "{noop}SOCIAL_LOGIN";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // ===== 카카오 파싱 =====
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
        String name = kakaoAccount == null ? null : (String) kakaoAccount.get("name");

        String kakaoGender = kakaoAccount == null ? null : (String) kakaoAccount.get("gender");
        String gender = "male".equals(kakaoGender) ? "M"
                : "female".equals(kakaoGender) ? "F" : null;

        String birthyear = kakaoAccount == null ? null : (String) kakaoAccount.get("birthyear");
        String birthday = kakaoAccount == null ? null : (String) kakaoAccount.get("birthday");
        String phoneNumber = kakaoAccount == null ? null : (String) kakaoAccount.get("phone_number");

        LocalDate birthDate = null;
        if (birthyear != null && birthday != null && birthday.length() == 4) {
            birthDate = LocalDate.of(
                    Integer.parseInt(birthyear),
                    Integer.parseInt(birthday.substring(0, 2)),
                    Integer.parseInt(birthday.substring(2, 4))
            );
        }

        // ===== 소셜 핵심 정보 =====
        String provider = "KAKAO";
        String providerId = String.valueOf(attributes.get("id"));

        // ✅ 세션에서 "강제 신규 생성" 플래그 확인
        HttpSession session = null;
        try {
            ServletRequestAttributes sra =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            session = sra.getRequest().getSession(false);
        } catch (Exception ignored) {}

        boolean forceNew = session != null
                && "Y".equals(session.getAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER));

        // 1️⃣ 소셜 계정 기준 조회
        SocialAccount social = socialAccountMapper.findByProviderAndProviderId(provider, providerId);

        User user;

        if (social != null) {
            // ✅ 이미 연동된 소셜 → 해당 user로 로그인
            user = userMapper.findByUserId(social.getUserId());
        } else {

            // 2️⃣ 아직 소셜 계정 없음
            // forceNew=Y면 이메일이 있어도 무조건 신규 생성(= user=null 처리)
            if (!forceNew) {
                user = (email == null) ? null : userMapper.findByEmail(email);
            } else {
                user = null;
            }

            if (user == null) {
                // 3️⃣ 신규 유저 생성
                String seed = (phoneNumber != null && !phoneNumber.isBlank())
                        ? phoneNumber
                        : UUID.randomUUID().toString();

                String userId;
                do {
                    userId = SocialUserIdGenerator.generate(provider, seed);
                } while (userMapper.existsByUserId(userId));

                String nickname;
                do {
                    nickname = NicknameGenerator.generate();
                } while (userMapper.existsByProfileNm(nickname));

                user = User.builder()
                        .userId(userId)
                        .email(email)
                        .userNm(name != null ? name : "소셜회원")
                        .profileNm(nickname)
                        .password(SOCIAL_PASSWORD)
                        .gender(gender)
                        .birthday(birthDate)
                        .phoneNo(phoneNumber)
                        .userRole(1)
                        .extraInfoDone("N")
                        .smsYn("N")
                        .userState("1")
                        .useImg(0)
                        .build();

                userMapper.insertUser(user);

                // 4️⃣ social_account 연동 insert
                SocialAccount newSocial = SocialAccount.builder()
                        .socialId(UUID.randomUUID().toString())
                        .userId(user.getUserId())
                        .provider(provider)
                        .providerId(providerId)
                        .build();

                socialAccountMapper.insertSocialAccount(newSocial);

                // ✅ 한번 신규 생성했으면 플래그 끄기
                if (session != null) {
                    session.setAttribute(SocialLinkSessionKey.FORCE_NEW_SOCIAL_USER, "N");
                }
            }
        }

        return new CustomOAuth2User(user, provider, providerId, email, attributes);
    }
}
