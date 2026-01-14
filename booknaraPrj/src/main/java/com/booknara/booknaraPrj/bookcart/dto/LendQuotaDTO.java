package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

@Data
public class LendQuotaDTO {
    private int maxLendCount;      // 최대 대여 가능
    private int currentLendCount;  // 현재 대여중
    private int cartCount;         // 장바구니 담긴 수
    private int availableCount;    // 남은 가능 권수
}

