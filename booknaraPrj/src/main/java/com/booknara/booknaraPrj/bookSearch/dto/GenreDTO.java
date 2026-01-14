package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

@Data
public class GenreDTO {
    private Integer genreId;     // GENRE_ID
    private String genreNm;      // GENRE_NM
    private Integer parentId;    // PARENT_ID (대분류면 null)
    private String mall; // 국내도서 / 외국도서
}
