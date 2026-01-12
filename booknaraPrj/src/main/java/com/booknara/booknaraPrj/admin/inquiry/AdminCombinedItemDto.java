package com.booknara.booknaraPrj.admin.inquiry;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCombinedItemDto {
    private String id;
    private String type;      // "INQUIRY" 또는 "REPORT"
    private String subType;   // 문의유형 또는 신고유형
    private String title;
    private String userId;
    private String state;     // "Y" 또는 "N" (통일)
    private LocalDateTime date;
    private String content;
    private String answer;    // 답변 내용
}


