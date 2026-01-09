package com.booknara.booknaraPrj.mypage.myinquiry;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MyInquiryMapper {

    List<MyInquiryHistoryDto> selectMyInquiry(
            @Param("userId") String userId,
            @Param("keyword") String keyword
    );

    void insertInquiry(MyInquiryWriteDto dto);
}
