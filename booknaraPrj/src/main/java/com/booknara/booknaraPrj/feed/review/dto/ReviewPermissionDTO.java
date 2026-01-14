package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

@Data
public class ReviewPermissionDTO {
    private String isbn13;

    // 반납 이력 기반 "리뷰 작성/수정 가능" 여부
    private String allowedYn;     // Y/N

    // 내 리뷰 존재 여부 (있으면 수정 모드)
    private String hasReviewYn;   // Y/N
    private String myFeedId;      // 내 리뷰 FEED_ID

    // 버튼/안내문구용(선택)
    private String message;
}
