package com.booknara.booknaraPrj.reviewstatus.dto;

import lombok.Data;

/**
 * [ReviewStatusDTO]
 * 특정 도서에 대한 리뷰 통계 정보를 전달하는 DTO입니다.
 * 별점 평균과 리뷰 총합을 통해 사용자에게 도서의 신뢰도 지표를 제공합니다.
 */
@Data
public class ReviewStatusDTO {
    /** 조회 대상 도서의 국제 표준 식별 번호 */
    private String isbn13;

    /** * [핵심] 도서 평균 별점 (예: 4.5 / 5.0)
     * - 리뷰가 하나도 없는 경우 null이 반환될 수 있으므로 Double 타입을 사용합니다.
     * - UI에서는 보통 소수점 첫째 자리까지 반올림하여 표시합니다.
     */
    private Double ratingAvg;

    /** * 등록된 총 리뷰 개수
     * - 리뷰가 없는 경우 0으로 표시되어 신규 도서임을 나타냅니다.
     */
    private Integer reviewCnt;
}