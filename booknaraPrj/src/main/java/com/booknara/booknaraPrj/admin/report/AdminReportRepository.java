package com.booknara.booknaraPrj.admin.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminReportRepository extends JpaRepository<AdminReport, String> {

    // 상태별 신고 목록 조회 (PENDING/RESOLVED)
    Page<AdminReport> findByAdminReportState(AdminReportState adminReportState, Pageable pageable);
    // 특정 사용자가 신고한 내역 조회
    List<AdminReport> findByUserId(String userId);

    // 미해결 신고 건수 조회
    long countByAdminReportState(AdminReportState adminReportState);}