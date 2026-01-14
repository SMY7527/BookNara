package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;

/**
 * [BookDetailDTO]
 * 도서 상세 정보 페이지를 렌더링하기 위한 데이터 전송 객체입니다.
 * DB의 여러 테이블(BOOK_ISBN, GENRE 등)에서 취합된 정보를 화면으로 전달합니다.
 */
@Data
public class BookDetailDTO {
    /** 도서 고유 식별자 (국제 표준 도서 번호 13자리) */
    private String isbn13;

    /** 도서 제목 */
    private String bookTitle;
    /** 저자 목록 (보통 '저자1^저자2' 형태로 저장되어 화면에서 ' · ' 등으로 치환 필요) */
    private String authors;
    /** 출판사 */
    private String publisher;

    /**
     * [출판일]
     * DB 저장 형식: VARCHAR(8) (예: 20160502)
     * UI 요구사항: "2016-05-02" 형식으로 변환하여 노출 권장
     */
    private String pubdate;

    /** 도서 줄거리 및 상세 설명 */
    private String description;

    /** 네이버 도서 API에서 제공하는 이미지 URL */
    private String naverImage;
    /** 알라딘 API에서 제공하는 큰 사이즈 이미지 URL (상세 페이지 메인용) */
    private String aladinImageBig;

    /** 전자책 여부 ('Y' 또는 'N') */
    private String ebookYn;
    /** 전자책 파일 정보 (epub 파일 경로 등, 종이책일 경우 null) */
    private String epub;

    /** 도서가 속한 장르의 고유 ID */
    private Integer genreId;
    /** [태그용] 장르명 (화면 하단이나 상단에 카테고리 태그로 노출) */
    private String genreNm;

}