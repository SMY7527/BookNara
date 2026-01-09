package com.booknara.booknaraPrj.mypage.myinquiry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyInquiryHistoryDto {

    private String inqId;
    private String inqTitle;
    private String inqContent;
    private String respContent;
    private LocalDateTime createdAt;
}
