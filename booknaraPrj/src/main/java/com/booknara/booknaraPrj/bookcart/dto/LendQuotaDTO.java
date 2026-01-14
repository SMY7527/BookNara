package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

/**
 * [LendQuotaDTO]
 * 사용자의 도서 대여 가능 한도(Quota) 정보를 담는 객체입니다.
 * 장바구니 화면이나 대결제 페이지에서 대여 가능 여부를 판단하는 기준이 됩니다.
 */
@Data
public class LendQuotaDTO {
    /** * [최대 대여 가능 권수]
     * 도서관 정책상 한 사용자가 동시에 가질 수 있는 최대 대여 한도입니다. (예: 5권)
     */
    private int maxLendCount;

    /** * [현재 대여중인 권수]
     * 사용자가 이미 빌려서 아직 반납하지 않은 도서의 수입니다.
     */
    private int currentLendCount;

    /** * [장바구니 담긴 수]
     * 현재 장바구니에 담겨 대여 대기 중인 도서의 수입니다.
     */
    private int cartCount;

    /** * [남은 대여 가능 권수]
     * 최종적으로 사용자가 지금 추가로 빌릴 수 있는 권수입니다.
     * 공식: maxLendCount - currentLendCount - cartCount
     */
    private int availableCount;
}