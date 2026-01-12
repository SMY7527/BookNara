package com.booknara.booknaraPrj.admin.bookManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookListResponseDto {
    private Long bookId;        // [추가]
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String description;
    private String publisher;
    private String pubDate;
    private String naverImage;
    private String aladinImageBig;
    private String eBookYn;
    private String epub;
    private String genreNm;
    private String bookState;
}