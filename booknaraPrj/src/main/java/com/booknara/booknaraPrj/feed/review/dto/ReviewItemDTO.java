package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewItemDTO {
    private String feedId;

    private String isbn13;

    private String userId;
    private String profileNm;     // USERS.PROFILE_NM (선택)
    private String profileImg;    // USERS.PROFILE_IMG (선택)
    private Integer useImg;       // USERS.USE_IMG (0/1) (선택)

    private Integer rate;         // REVIEW_DETAIL.RATE

    private String title;         // FEEDS.FEED_TITLE
    private String content;       // FEEDS.CONTENT

    private LocalDateTime createdAt;
    private String mineYn;   // "Y" / "N"

}
