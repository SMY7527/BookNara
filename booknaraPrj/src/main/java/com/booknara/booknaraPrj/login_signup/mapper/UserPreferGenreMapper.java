package com.booknara.booknaraPrj.login_signup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserPreferGenreMapper {
    int insert(@Param("userId") String userId,
               @Param("genreId") Integer genreId);
    List<Integer> findActiveGenreIdsByUserId(@Param("userId") String userId);
}
