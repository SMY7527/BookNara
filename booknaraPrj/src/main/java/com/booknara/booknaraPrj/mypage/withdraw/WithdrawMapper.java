package com.booknara.booknaraPrj.mypage.withdraw;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WithdrawMapper {

    String selectPassword(@Param("userId") String userId);

    void updateUserState(@Param("userId") String userId,
                         @Param("state") String state);
}
