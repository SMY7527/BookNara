package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MyLibraryDto {

    private String lendId;
    private Long bookId;
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String coverImg;

    private LocalDateTime lendDate;
    private LocalDateTime returnDueDate;
    private String overDue; // Y / N
}
