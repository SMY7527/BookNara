package com.booknara.booknaraPrj.report.dto;

import lombok.Data;

@Data
public class ReportCreateDTO {
    private String feedId;        // 신고 대상 리뷰
    private String reportContent; // 신고 사유
}
