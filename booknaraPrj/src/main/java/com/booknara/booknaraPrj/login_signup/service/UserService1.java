package com.booknara.booknaraPrj.login_signup.service;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.dto.SignupRequest;
import com.booknara.booknaraPrj.login_signup.dto.SocialAccount;
import com.booknara.booknaraPrj.login_signup.mapper.SocialAccountMapper;
import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.booknara.booknaraPrj.security.oauth.SocialLinkSessionKey.*;

@Service
@RequiredArgsConstructor
public class UserService1 {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountMapper socialAccountMapper;
    //회원가입
    @Transactional
    public void signup(SignupRequest req) {

        // 서버 유효성 검사
        validateSignup(
                req.getUserId(),
                req.getPassword(),
                req.getUserNm(),
                req.getEmail()
        );

        User user = new User();
        user.setUserId(req.getUserId());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setUserNm(req.getUserNm());
        user.setProfileNm(req.getProfileNm());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setPhoneNo(req.getPhoneNo());
        user.setEmail(req.getEmail());

        user.setExtraInfoDone("N");
        user.setUserRole(1);
        user.setUserState("1");
        user.setUseImg(0);

        user.setSmsYn(req.getSmsYn() != null ? "Y" : "N");

        user.setUserRole(1);
        user.setUserState("1");
        user.setUseImg(0);


        userMapper.insertUser(user);
    }


    //유효성검사
    private void validateSignup(String userId,
                                String password,
                                String name,
                                String email) {

        // 1. 아이디 필수 + 형식
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수여유");
        }

        if (!userId.matches("^[a-zA-Z0-9]{4,12}$")) {
            throw new IllegalArgumentException("아이디 형식이 올바르지 않아유");
        }

        // 2. 비밀번호 규칙
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수여유");
        }

        String pwRegex =
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$";

        if (!password.matches(pwRegex)) {
            throw new IllegalArgumentException("비밀번호 규칙이 안 맞아유");
        }

        // 3. 이름 필수
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수여유");
        }

        // 4. 아이디 중복 (⭐ 서버에서 최종)
        if (userMapper.countByUserId(userId) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디여유");
        }

        // 5. 이메일 (선택이면 형식만)
        if (email != null && !email.isBlank()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("이메일 형식이 이상해유");
            }
        }

    }

    public boolean isUserIdAvailable(String userId) {
        return userMapper.countByUserId(userId) == 0;
    }

    public boolean isProfileNameAvailable(String profileNm) {
        return userMapper.countByProfileNm(profileNm) == 0;
    }

    public boolean isEmailAvailable(String email) {
        return userMapper.countByEmail(email) == 0;
    }


    //로그인
    public User login(String userId, String rawPassword) {
        User user = userMapper.findByUserId(userId);

        if (user == null) {
            throw new IllegalArgumentException("아이디가 없어요");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀려유");
        }

        return user;
    }

    public void updateExtraInfoDone(String userId) {
        userMapper.updateExtraInfoDone(userId);
    }

    public void updateAddress(String userId,
                              String zipcode,
                              String addr,
                              String detailAddr) {

        userMapper.updateAddress(userId, zipcode, addr, detailAddr);
    }

    @Transactional
    public String signupSocial(HttpSession session) {

        String provider = (String) session.getAttribute(LINK_PROVIDER);
        String providerId = (String) session.getAttribute(LINK_PROVIDER_ID);
        String email = (String) session.getAttribute(LINK_EMAIL);

        if (provider == null || providerId == null || email == null) {
            throw new IllegalStateException("소셜 정보 만료");
        }

        // 이미 연동돼 있으면 차단
        SocialAccount already =
                socialAccountMapper.findByProviderAndProviderId(provider, providerId);
        if (already != null) {
            return already.getUserId();
        }

        // 새 USER 생성
        String userId;
        do {
            userId = SocialUserIdGenerator.generate(provider, null);
        } while (userMapper.existsByUserId(userId));

        String nickname;
        do {
            nickname = NicknameGenerator.generate();
        } while (userMapper.existsByProfileNm(nickname));

        User user = User.builder()
                .userId(userId)
                .email(email)
                .profileNm(nickname)
                .password("{noop}SOCIAL_LOGIN")
                .userRole(1)
                .extraInfoDone("N")
                .smsYn("N")
                .userState("1")
                .useImg(0)
                .build();

        userMapper.insertUser(user);

        SocialAccount sa = SocialAccount.builder()
                .socialId(UUID.randomUUID().toString())
                .userId(userId)
                .provider(provider)
                .providerId(providerId)
                .build();

        socialAccountMapper.insertSocialAccount(sa);

        return userId;
    }



    public String findUserId(String name, String email) {
        return userMapper.findLocalUserIdByNameAndEmail(name, email);
    }

    public boolean checkUserForPasswordReset(String userId, String email) {
        return userMapper.countByUserIdAndEmail(userId, email) == 1;
    }

    public String createVerifyCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    public void resetPassword(String userId, String password) {

        // 비밀번호 유효성 검사
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$";
        if (!password.matches(regex)) {
            throw new IllegalArgumentException("비밀번호 형식 오류");
        }

        String encoded = passwordEncoder.encode(password);

        int updated = userMapper.updatePassword(userId, encoded);
        if (updated != 1) {
            throw new IllegalStateException("비밀번호 변경 실패");
        }
    }

    //mypage에서 로그인정보 조회하기위해 추가
    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userMapper.findByUserId(userId);
    }

}
