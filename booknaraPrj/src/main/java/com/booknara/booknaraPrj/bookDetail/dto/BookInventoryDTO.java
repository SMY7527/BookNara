package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;

@Data
public class BookInventoryDTO {

    // BOOKS 총 소장 수 (ISBN 기준)

    private long totalCount;

    //보유중인 도서량 BOOK_STATE='N'(도서 상태 정상)
    private long availableCount;

    //분실/만료 - BOOK_STATE='L'
    private long lostCount;
}
