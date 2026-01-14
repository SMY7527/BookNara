package com.booknara.booknaraPrj.bookAPI.client.naver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [NaverDTO]
 * 네이버 도서 검색 API의 개별 아이템 정보를 매핑하는 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverDTO {

    /** 도서 표지 이미지 URL (DB의 NAVER_IMAGE 컬럼에 대응) */
    private String image;

    /** 저자 정보 (여러 명일 경우 문자열로 수신, DB의 AUTHORS 컬럼 대응) */
    private String author;

    /** 도서 상세 설명 (DB의 DESCRIPTION 컬럼 보완용) */
    private String description;
}