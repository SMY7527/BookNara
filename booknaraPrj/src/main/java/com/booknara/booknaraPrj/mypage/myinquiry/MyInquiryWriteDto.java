package com.booknara.booknaraPrj.mypage.myinquiry;

import lombok.Data;

@Data
public class MyInquiryWriteDto {

    private String userId;
    private String inqTitle;
    private String inqContent;
    private int inqType; // 1~6
}
