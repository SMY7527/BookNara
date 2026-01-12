package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;

@Data
public class BookDetailDTO {
    private String isbn13;

    private String bookTitle;
    private String authors;
    private String publisher;

    /**
     * BOOK_ISBN.PUBDATE: VARCHAR(8) (예: 20160502)
     * 화면에서 YYYY-MM-DD로 파싱 필요.
     */
    private String pubdate;

    private String description;

    private String naverImage;
    private String aladinImageBig;

    private String ebookYn; // 'Y' or 'N'
    private String epub;    // nullable

    private Integer genreId;
    private String genreNm; // GENRE.GENRE_NM (태그용)

}
