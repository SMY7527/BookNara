package com.booknara.booknaraPrj.login_signup.mapper;

import com.booknara.booknaraPrj.login_signup.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface UserMapper {
    // 회원가입
    int insertUser(User user);

    // 로그인용 조회
    User findByUserId(String userId);

    // 아이디 중복 체크
    int countByUserId(String userId);

    // 프로필명 중복 체크
    int countByProfileNm(String profileNm);

    //이메일 조회
    User findByEmail(String email);

    //이메일중복체크
    int countByEmail(String email);

    //소셜 로그인시 id생성할때 중복체크
    boolean existsByUserId(String userId);

    //닉네임 자동생성시 중복체크
    boolean existsByProfileNm(String nickname);


    void updateExtraInfoDone(String userId);

    void updateAddress(@Param("userId") String userId,
                       @Param("zipcode") String zipcode,
                       @Param("addr") String addr,
                       @Param("detailAddr") String detailAddr);

    String findPasswordByUserId(@Param("userId") String userId);

    //아이디비밀번호 찾기
    String findLocalUserIdByNameAndEmail(
            @Param("name") String name,
            @Param("email") String email
    );

    int countByUserIdAndEmail(@Param("userId") String userId,
                              @Param("email") String email);


    int updatePassword(@Param("userId") String userId,
                       @Param("password") String password);
}


