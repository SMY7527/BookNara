package com.booknara.booknaraPrj.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookIsbnTempDTO {

    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;
    private String genreId;
    private String description;
    private LocalDate pubdate;
    private String image;
    private LocalDateTime infonaruFetchedAt; // INFONARU_FETCHED_AT
    private LocalDateTime naverFetchedAt;    // NAVER_FETCHED_AT
    private LocalDateTime aladinFetchedAt;   // ALADIN_FETCHED_AT
    private String category_no;
    private String data_hash;
    private int statusCd;
}
