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

    private String isbn13;                 // ISBN13

    private String bookTitle;              // BOOK_TITLE
    private String authors;                // AUTHORS
    private String publisher;              // PUBLISHER
    private Integer genreId;               // GENRE_ID (INT)

    private String description;            // DESCRIPTION
    private LocalDate pubdate;             // PUBDATE

    private String naverImage;             // NAVER_IMAGE
    private String aladinImageBig;         // ALADIN_IMAGE_BIG

    private String naverHash;              // NAVER_HASH (CHAR(64))

    private LocalDateTime infonaruFetchedAt; // INFONARU_FETCHED_AT
    private LocalDateTime naverFetchedAt;    // NAVER_FETCHED_AT
    private LocalDateTime aladinFetchedAt;   // ALADIN_FETCHED_AT

    private Integer statusCd;              // STATUS_CD (TINYINT)
}
