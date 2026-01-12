package com.booknara.booknaraPrj.login_signup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMallangMapper {

    void insertRandomMallang(@Param("userId") String userId);

    String selectProfileImage(@Param("userId") String userId);

}

