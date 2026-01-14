package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;

/**
 * [BookInventoryDTO]
 * 특정 도서(ISBN13)에 속한 개별 실물 도서들의 현황을 집계한 객체입니다.
 * 도서관의 자산 관리 및 사용자의 대출 가능 여부 판단에 사용됩니다.
 */
@Data
public class BookInventoryDTO {

    /** * [총 소장 권수]
     * 해당 ISBN으로 등록된 도서관의 모든 실물 장서 수입니다.
     */
    private long totalCount;

    /** * [대출 가용 권수 (정상 상태)]
     * BOOK_STATE = 'N' (Normal)
     * 현재 서가에 비치되어 있거나, 상태가 양호하여 대출이 가능한 도서의 수입니다.
     */
    private long availableCount;

    /** * [손실 권수 (분실/폐기)]
     * BOOK_STATE = 'L' (Lost)
     * 실물은 존재하지 않거나, 파손/기한 만료로 인해 서비스에서 제외된 도서의 수입니다.
     */
    private long lostCount;
}