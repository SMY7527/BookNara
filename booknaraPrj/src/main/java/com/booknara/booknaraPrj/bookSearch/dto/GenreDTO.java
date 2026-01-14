package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

/**
 * [GenreDTO]
 * 도서의 장르(카테고리) 정보를 담는 데이터 전송 객체입니다.
 * 국내/외국 도서 구분과 상위-하위 장르 간의 계층 구조를 표현합니다.
 */
@Data
public class GenreDTO {
    /** 장르 고유 식별자 (DB의 GENRE_ID) */
    private Integer genreId;

    /** 장르 명칭 (예: 소설, 경제경영, 프로그래밍 등) */
    private String genreNm;

    /** * 상위 장르 식별자
     * - 대분류(최상위 장르)인 경우 null 값을 가집니다.
     * - 소분류인 경우 소속된 대분류의 genreId를 가집니다.
     */
    private Integer parentId;

    /** * 도서 분류 체계 구분
     * - '국내도서' 또는 '외국도서' 값을 가집니다.
     */
    private String mall;
}