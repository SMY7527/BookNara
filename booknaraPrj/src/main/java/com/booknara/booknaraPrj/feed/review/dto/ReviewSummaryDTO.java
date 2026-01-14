package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

/**
 * [ReviewSummaryDTO]
 * 특정 도서의 리뷰 통계 정보를 담는 데이터 전송 객체입니다.
 * 도서 상세 상단이나 목록의 별점 표시 컴포넌트에서 주로 사용됩니다.
 */
@Data
public class ReviewSummaryDTO {
    /** 조회 대상 도서의 고유 식별자(ISBN13) */
    private String isbn13;

    /** * [총 리뷰 개수]
     * 해당 도서에 작성된 전체 리뷰의 수입니다.
     * null 가능성이 있으므로, 서비스 레이어에서 0으로 초기화하는 방어 코드가 권장됩니다.
     */
    private Integer reviewCnt;

    /** * [평균 평점]
     * 작성된 별점들의 평균값입니다. (예: 4.5)
     * UI 렌더링 시 별(Star) 아이콘의 너비를 결정하는 기준이 됩니다.
     * null일 경우 0.0으로 처리하여 계산 오류를 방지해야 합니다.
     */
    private Double ratingAvg;
}