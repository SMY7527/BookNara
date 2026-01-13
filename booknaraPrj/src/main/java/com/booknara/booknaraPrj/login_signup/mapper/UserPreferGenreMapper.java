package com.booknara.booknaraPrj.login_signup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserPreferGenreMapper {

    // 회원가입 / 신규 장르 저장
    int insert(@Param("userId") String userId,
               @Param("genreId") Integer genreId);

    // 로그인 / 마이페이지 공용 - 활성 장르 조회
    List<Integer> findActiveGenreIdsByUserId(
            @Param("userId") String userId
    );

    // 마이페이지 수정용 - 전체 비활성화
    int deactivateAllByUserId(
            @Param("userId") String userId
    );

    // 마이페이지 수정용 - 기존 장르 재활성화
    int activateGenre(@Param("userId") String userId,
                      @Param("genreId") Integer genreId);

}
