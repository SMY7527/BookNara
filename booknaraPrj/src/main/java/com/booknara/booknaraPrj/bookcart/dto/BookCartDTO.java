package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

/**
 * [BookCartDTO]
 * 사용자의 장바구니에 담긴 도서 정보를 관리하는 데이터 전송 객체입니다.
 * 장바구니 목록 화면을 구성하기 위해 도서 메타데이터와 실시간 대여 가능 여부를 포함합니다.
 */
@Data
public class BookCartDTO {
    /** 장바구니 항목 고유 번호 (PK) */
    private Long cartId;

    /** 도서 고유 식별자 (ISBN13) */
    private String isbn13;

    /** 도서 제목 */
    private String bookTitle;

    /** 저자 정보 (화면 노출 시 '^' 구분자 처리 필요) */
    private String authors;

    /** 네이버 도서 이미지 URL */
    private String naverImage;

    /** 알라딘 제공 대형 이미지 URL */
    private String aladinImageBig;

    /** 전자책 여부 ('Y' 또는 'N') */
    private String ebookYn;

    /** * [실시간 대여 가능 여부]
     * 도서관에 대여 가능한 실물 재고가 있는지 여부를 판단하는 플래그입니다.
     * true: 즉시 대여 가능 / false: 모두 대여 중이거나 예약 필요
     */
    private Boolean lendableYn;
}