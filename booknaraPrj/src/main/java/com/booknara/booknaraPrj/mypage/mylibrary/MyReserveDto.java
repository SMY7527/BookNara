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
}
