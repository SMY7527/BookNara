package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

@Data
public class BookSearchDTO {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;


    private String pubdate;//VARCHAR(8) (예: 20160502)
    private String description;

    private String naverImage;
    private String aladinImageBig;
    private String ebookYn;   // 'Y'/'N'

    private Integer genreId;
    private String genreNm;


}
