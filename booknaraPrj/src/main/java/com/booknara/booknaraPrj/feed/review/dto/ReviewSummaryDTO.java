package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

@Data
public class ReviewSummaryDTO {
    private String isbn13;
    private Integer reviewCnt;   // null 가능 -> 서비스에서 0 처리 권장
    private Double ratingAvg;    // null 가능 -> 서비스에서 0.0 처리 권장
}
