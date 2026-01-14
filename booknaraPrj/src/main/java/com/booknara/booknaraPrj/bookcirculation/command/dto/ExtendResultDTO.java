package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [ExtendResultDTO]
 * 도서 대출 연장 처리 결과를 반환하는 데이터 전송 객체입니다.
 * 성공 여부와 함께 사용자에게 노출할 메시지를 포함합니다.
 */
@Data
public class ExtendResultDTO {

    /** * 연장 처리가 시도된 대출 고유 번호 (LEND_ID)
     * 어떤 대출 건에 대한 응답인지 식별하기 위해 사용됩니다.
     */
    private String lendId;

    /** * 연장 성공 여부 플래그 ('Y' / 'N')
     * 시스템 정책에 따라 정상적으로 반납 예정일이 갱신된 경우 'Y'를 반환합니다.
     */
    private String extendedYn; // 'Y'/'N'

    /** * 처리 결과 메시지
     * 성공 시: "대출 기간이 7일 연장되었습니다."
     * 실패 시: "이미 연장하신 도서입니다.", "반납 7일 전부터 연장이 가능합니다." 등
     * 이 메시지는 프론트엔드에서 알럿(Alert)이나 토스트 메시지로 활용됩니다.
     */
    private String message;
}