package com.booknara.booknaraPrj.mypage.info;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyInfoMapper {

    MyInfoDto selectMyInfo(@Param("userId") String userId);

    void updateMyInfo(MyInfoDto dto);
}

