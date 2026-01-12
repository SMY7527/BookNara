package com.booknara.booknaraPrj.mypage.mylibrary;
// 예약목록
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyReserveDto {
    private String rsvId;
    private String isbn13;

    private String bookTitle;
    private String authors;
    private String coverImg;

    private LocalDateTime rsvDate;


    // ✅ “예상 대출 가능일”(사진처럼)
    // 일단 간단히 rsvDate + 3일 같은 계산으로도 가능
    private LocalDateTime availableAt;

}
