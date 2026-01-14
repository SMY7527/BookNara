package com.booknara.booknaraPrj.report.service;

import com.booknara.booknaraPrj.report.dto.ReportCreateDTO;
import com.booknara.booknaraPrj.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [ReportService]
 * 도서 리뷰에 대한 사용자 신고를 처리하고 검증하는 서비스입니다.
 * 깨끗한 커뮤니티 환경 조성을 위한 비즈니스 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional // 모든 쓰기 작업에 트랜잭션 보장
public class ReportService {

    private final ReportMapper reportMapper;

    /** [조회] 특정 유저가 해당 피드를 이미 신고했는지 확인 */
    @Transactional(readOnly = true)
    public boolean hasReported(String userId, String feedId) {
        return reportMapper.existsReport(userId, feedId) == 1;
    }

    /**
     * [신고 생성]
     * 리뷰 신고를 접수하기 전 3단계 검증을 거친 후 저장합니다.
     */
    public void createReport(String userId, ReportCreateDTO dto) {
        String feedId = dto.getFeedId();

        // [검증 0] 대상 피드의 유효성 확인
        // 신고하는 도중 게시글이 삭제되거나 블라인드 처리된 경우를 방어합니다.
        if (reportMapper.existsActiveReviewFeed(feedId) != 1) {
            throw new IllegalArgumentException("존재하지 않거나 이미 처리된 리뷰입니다.");
        }

        // [검증 1] 자기 글 신고 금지 정책
        // 본인이 쓴 글을 스스로 신고하여 시스템을 교란하는 행위를 차단합니다.
        String ownerId = reportMapper.selectFeedOwnerUserId(feedId);
        if (ownerId != null && ownerId.equals(userId)) {
            throw new IllegalStateException("본인이 작성한 리뷰는 신고할 수 없습니다.");
        }

        // [검증 2] 기존 신고 여부 1차 체크 (UX 최적화)
        if (hasReported(userId, feedId)) {
            throw new IllegalStateException("이미 신고한 리뷰입니다.");
        }

        // [실행] 신고 ID 생성 및 DB 저장
        String reportId = genReportId();
        try {
            reportMapper.insertReport(reportId, userId, feedId, dto.getReportContent());
        } catch (DuplicateKeyException e) {
            // [검증 3] 동시성 이슈 방어 (Database Level)
            // 아주 짧은 찰나에 두 번 클릭되어 1차 체크를 통과하더라도,
            // DB의 Unique 제약조건(User+Feed)을 통해 최종적으로 중복 저장을 막습니다.
            throw new IllegalStateException("이미 신고 처리 중인 리뷰입니다.");
        }
    }

    /**
     * [신고 ID 생성기]
     * 추적성을 높이기 위해 'R_날짜시간_랜덤값' 조합으로 고유 번호를 생성합니다.
     */
    private String genReportId() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String rnd = Integer.toString((int)(Math.random() * 9000) + 1000);
        return "R_" + ts + "_" + rnd;
    }
}