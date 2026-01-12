package com.booknara.booknaraPrj.admin.report;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REPORT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminReport {

    @Id
    @Column(name = "REPORT_ID", length = 30)
    private String reportId;

    @Column(name = "USER_ID", length = 50, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "REPORT_TYPE", length = 20, nullable = false)
    private AdminReportType adminReportType;

    @Column(name = "REPORT_CONTENT", length = 500, nullable = false)
    private String reportContent;


    @Column(name = "RESOLVED_CONTENT", length = 500)
    private String resolvedContent;

    @Column(name = "REPORTED_AT", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "REPORT_STATE", length = 20, nullable = false)
    private AdminReportState adminReportState;

    @Builder
    public AdminReport(String reportId, String userId, AdminReportType adminReportType, String reportContent) {
        this.reportId = reportId;
        this.userId = userId;
        this.adminReportType = adminReportType;
        this.reportContent = reportContent;
        this.reportedAt = LocalDateTime.now();
        this.adminReportState = AdminReportState.PENDING; // 기본값 세팅
    }

    // 신고 처리 완료 메서드
    public void resolve(String resolvedContent) {
        this.resolvedContent = resolvedContent;
        this.adminReportState = AdminReportState.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
}