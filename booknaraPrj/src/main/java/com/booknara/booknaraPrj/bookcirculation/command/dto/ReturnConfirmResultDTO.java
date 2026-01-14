package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [ReturnConfirmResultDTO]
 * 사서의 실물 확인 또는 시스템 최종 검수를 통해 도서 반납이 확정된 결과를 담는 DTO입니다.
 * 이 응답은 대출 프로세스의 완전한 종료를 의미합니다.
 */
@Data
public class ReturnConfirmResultDTO {

    /** * 반납 확정 처리가 완료된 대출 고유 번호 (LEND_ID)
     * 어떤 대출 건이 최종적으로 종결되었는지 식별합니다.
     */
    private String lendId;

    /** * 최종 반납 확정 여부 플래그 ('Y' / 'N')
     * DB에서 대출 정보가 '반납 완료' 상태로 변경되고,
     * 실물 도서가 '대출 가능' 상태로 복구되었을 때 'Y'를 반환합니다.
     */
    private String returnedYn; // 'Y'/'N'
}