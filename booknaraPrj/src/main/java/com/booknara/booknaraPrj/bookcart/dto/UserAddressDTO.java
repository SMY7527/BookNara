package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

/**
 * [UserAddressDTO]
 * 사용자의 주소 정보를 관리하기 위한 객체입니다.
 * 장바구니 결제(대여 신청) 시 배송지 확인이나 회원 정보 수정 시 사용됩니다.
 */
@Data
public class UserAddressDTO {
    /** 회원 고유 식별자 (DB의 PK와 매핑되어 Update 쿼리의 조건절에 사용됨) */
    private String userId;

    /** 우편번호 (5자리 신형 우편번호 규격) */
    private String zipcode;

    /** 기본 주소 (도로명 주소 또는 지번 주소) */
    private String addr;

    /** 상세 주소 (동/호수, 건물명 등 사용자가 직접 입력하는 정보) */
    private String detailAddr;
}