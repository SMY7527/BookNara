package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

/**
 * [LendResultDTO]
 * 도서 대출 처리가 완료된 후 생성된 대출 정보를 담는 응답 DTO입니다.
 * 성공한 대출 건에 대해 영수증과 같은 역할을 수행합니다.
 */
@Data
public class LendResultDTO {

    /** * 생성된 대출 고유 번호 (LEND_ID)
     * LENDS 테이블의 PK로, 향후 반납이나 연장 시 식별자로 사용됩니다.
     */
    private String lendId;

    /** * 대출된 실물 도서 식별자 (BOOK_ID)
     * 동일 ISBN 도서 중 실제로 대출 처리된 특정 '권'의 식별 번호입니다.
     */
    private Long bookId;

    /** * 대출된 도서의 국제 표준 도서 번호 (ISBN13)
     * 도서 상세 정보와 매핑하거나 UI에서 도서 이미지를 표시할 때 사용됩니다.
     */
    private String isbn13;
}