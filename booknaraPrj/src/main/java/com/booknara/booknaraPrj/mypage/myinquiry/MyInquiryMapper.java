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

    int updateInquiryFiles(
            @Param("inqId") String inqId,
            @Param("filePath1") String filePath1,
            @Param("filePath2") String filePath2,
            @Param("filePath3") String filePath3
    );
}

