package com.booknara.booknaraPrj.report.service;

import com.booknara.booknaraPrj.report.dto.ReportCreateDTO;
import com.booknara.booknaraPrj.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportMapper reportMapper;

    /** 이미 신고했는지(1=Y, 0=N) */
    @Transactional(readOnly = true)
    public boolean hasReported(String userId, String feedId) {
        return reportMapper.existsReport(userId, feedId) == 1;
    }

    /** 리뷰 신고 생성 */
    public void createReport(String userId, ReportCreateDTO dto) {
        String feedId = dto.getFeedId();

        // 0) 대상 피드 유효성(리뷰/미삭제)
        if (reportMapper.existsActiveReviewFeed(feedId) != 1) {
            throw new IllegalArgumentException("존재하지 않거나 삭제된 리뷰입니다.");
        }

        // 1) 이미 신고했는지 빠른 체크
        if (hasReported(userId, feedId)) {
            throw new IllegalStateException("이미 신고한 리뷰입니다.");
        }

        // 2) INSERT (동시성 대비: 유니크키로 최종 차단)
        String reportId = genReportId();
        try {
            reportMapper.insertReport(reportId, userId, feedId, dto.getReportContent());
        } catch (DuplicateKeyException e) {
            // UQ_REPORT_USER_FEED 위반
            throw new IllegalStateException("이미 신고한 리뷰입니다.");
        }
    }

    private String genReportId() {
        // 예: R_YYYYMMDDHHMMSS_XXXX (팀 규칙에 맞게 바꿔도 됨)
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String rnd = Integer.toString((int)(Math.random() * 9000) + 1000);
        return "R_" + ts + "_" + rnd;
    }
}
