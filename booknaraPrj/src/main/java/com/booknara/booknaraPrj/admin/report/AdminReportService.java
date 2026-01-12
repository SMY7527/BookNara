package com.booknara.booknaraPrj.admin.report;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final AdminReportRepository adminReportRepository;

    public long getTotalCount() { return adminReportRepository.count(); }
    public long getPendingCount() { return adminReportRepository.countByAdminReportState(AdminReportState.PENDING); }
    public long getResolvedCount() { return adminReportRepository.countByAdminReportState(AdminReportState.RESOLVED); }

    // 페이징 처리 메소드 (예시)
    public Page<AdminReport> getReports(String type, String status, Pageable pageable) {
        // 1. 유형 필터: INQUIRY가 선택되었다면 신고 내역은 보여줄 필요 없음
        if ("INQUIRY".equals(type)) return Page.empty(pageable);

        // 2. 상태 필터 변환: String -> Enum
        AdminReportState dbStatus = null;
        try {
            if (!"ALL".equals(status)) {
                dbStatus = AdminReportState.valueOf(status); // PENDING 또는 RESOLVED
            }
        } catch (IllegalArgumentException e) {
            dbStatus = null;
        }

        // 3. 필터에 따른 조회
        if (dbStatus == null) return adminReportRepository.findAll(pageable);
        return adminReportRepository.findByAdminReportState(dbStatus, pageable);
    }

    // 1. 단건 조회 메서드
    public AdminReport getReport(String id) {
        return adminReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역이 없습니다. id=" + id));
    }

    // 2. 저장 메서드
    @Transactional
    public void save(AdminReport adminReport) {
        adminReportRepository.save(adminReport);
    }
}
