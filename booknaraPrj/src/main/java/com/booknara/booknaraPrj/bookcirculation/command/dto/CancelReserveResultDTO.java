package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [CancelReserveResultDTO]
 * 도서 예약 취소 요청에 대한 처리 결과를 담는 DTO입니다.
 * 주로 서비스 레이어에서 취소 로직 수행 후, 컨트롤러를 거쳐 클라이언트로 반환됩니다.
 */
@Data
public class CancelReserveResultDTO {

    /** * 취소 처리된 예약 고유 번호 (RSV_ID)
     * 어떤 항목이 삭제/취소되었는지 클라이언트가 식별하기 위해 사용됩니다.
     */
    private String rsvId;

    /** * 취소 성공 여부 플래그 ('Y' / 'N')
     * 로직상 정상적으로 취소 상태로 변경되었거나 데이터가 삭제된 경우 'Y'를 반환합니다.
     */
    private String cancelledYn; // 'Y'/'N'
}