package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewListDTO {
    private String isbn13;
    private ReviewSummaryDTO summary;
    private List<ReviewItemDTO> items;

    private int page;
    private int size;
    private long total; // count
}
