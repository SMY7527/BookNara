package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.Data;
// 북마크
import java.time.LocalDateTime;

@Data
public class MyBookmarkDto {
    private String isbn13;

    private String bookTitle;
    private String authors;
    private String coverImg;

    private String bookmarkYn; // Y/N
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
