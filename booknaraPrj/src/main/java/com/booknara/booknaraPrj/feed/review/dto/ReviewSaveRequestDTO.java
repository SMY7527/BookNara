package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

@Data
public class ReviewSaveRequestDTO {
    private String isbn13;
    private Integer rate;   // 1~5
    private String title;
    private String content;
}
