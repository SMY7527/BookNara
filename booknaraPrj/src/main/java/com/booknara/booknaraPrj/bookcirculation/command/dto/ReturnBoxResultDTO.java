package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [ReturnBoxResultDTO]
 * 도서 무인 반납함 투입 시 처리 결과를 반환하는 DTO입니다.
 * 사용자가 물리적으로 반납함에 도서를 넣었음을 시스템상으로 1차 확인해주는 역할을 합니다.
 */
@Data
public class ReturnBoxResultDTO {

    /** * 반납 처리 대상 대출 고유 번호 (LEND_ID)
     * 어떤 대출 건이 반납함으로 인입되었는지 식별합니다.
     */
    private String lendId;

    /** * 반납함 투입 성공 여부 플래그 ('Y' / 'N')
     * 스마트 반납함 등에서 도서 인식이 성공하고 투입구가 정상적으로 닫혔을 때 'Y'가 반환됩니다.
     */
    private String boxedYn; // 'Y'/'N'
}