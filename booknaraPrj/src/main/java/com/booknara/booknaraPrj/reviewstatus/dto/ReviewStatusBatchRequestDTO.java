package com.booknara.booknaraPrj.reviewstatus.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewStatusBatchRequestDTO {
    private List<String> isbn13List;
}
