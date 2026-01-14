package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [ReserveResultDTO]
 * 도서 예약 신청이 성공적으로 완료된 후, 생성된 예약 정보를 반환하는 DTO입니다.
 * 사용자가 예약 목록을 확인하거나 취소할 때 필요한 식별 데이터를 포함합니다.
 */
@Data
public class ReserveResultDTO {

    /** * 생성된 예약 고유 번호 (RSV_ID)
     * RESERVATIONS 테이블의 PK이며, 예약 취소나 우선순위 확인 시 식별자로 사용됩니다.
     */
    private String rsvId;

    /** * 예약된 도서의 국제 표준 도서 번호 (ISBN13)
     * 어떤 도서에 대해 예약 대기가 걸렸는지 확인하기 위해 사용됩니다.
     */
    private String isbn13;
}