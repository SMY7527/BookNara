package com.booknara.booknaraPrj.admin.bookMangement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookListResponseDto {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String description;
    private String publisher;
    private LocalDate pubDate;
    private String aladinImageBig;
    private String eBookYn;
    private String epub;
    private String genreNm;
    private String bookState;
}