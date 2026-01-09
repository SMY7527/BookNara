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
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String description;
    private String publisher;

    // 엔터티 타입 변경(String)에 맞춰 수정
    private String pubDate;

    private String naverImage;      // 추가된 필드
    private String aladinImageBig;
    private String eBookYn;
    private String epub;

    // 연관 관계인 Genre 엔터티에서 가져올 이름
    private String genreNm;

    // 도서 상태 (필요에 따라 로직으로 생성)
    private String bookState;

}