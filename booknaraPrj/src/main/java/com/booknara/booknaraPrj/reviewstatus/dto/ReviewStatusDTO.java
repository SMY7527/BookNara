package com.booknara.booknaraPrj.reviewstatus.dto;

import lombok.Data;

@Data
public class ReviewStatusDTO {
    private String isbn13;
    private Double ratingAvg;   // 평균 별점 (null 가능)
    private Integer reviewCnt;  // 리뷰 개수 (0 가능)
}
