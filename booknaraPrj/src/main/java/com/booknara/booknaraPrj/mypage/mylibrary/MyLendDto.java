package com.booknara.booknaraPrj.mypage.mylibrary;

// 대여목록
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyLendDto {
    private String lendId;
    private Long bookId;
    private String isbn13;

    private String bookTitle;
    private String authors;
    private String coverImg;

    private LocalDateTime lendDate;
    private LocalDateTime returnDueDate;
    private LocalDateTime returnDoneAt;

    private Integer extendCnt;

    private String overDue; // Y/N
}
