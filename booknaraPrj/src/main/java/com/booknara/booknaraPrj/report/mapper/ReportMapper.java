package com.booknara.booknaraPrj.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * [ReportMapper]
 * 부적절한 리뷰 및 피드에 대한 신고 내역을 DB에 기록하고 검증하는 역할을 수행합니다.
 * 커뮤니티 가이드라인 준수를 위한 운영 관리 데이터의 창구입니다.
 */
@Mapper
public interface ReportMapper {

    /**
     * [신고 대상 유효성 확인]
     * 신고하려는 피드가 실제로 존재하며, 삭제되지 않은 활성 상태인지 확인합니다.
     * @return 존재하면 1, 아니면 0
     */
    int existsActiveReviewFeed(@Param("feedId") String feedId);

    /**
     * [중복 신고 방지]
     * 동일한 사용자가 특정 피드에 대해 이미 신고를 완료했는지 확인합니다. (1인 1신고 원칙)
     * @return 이미 신고했으면 1, 아니면 0
     */
    int existsReport(@Param("userId") String userId,
                     @Param("feedId") String feedId);

    /**
     * [신고 내역 등록]
     * 검증이 완료된 신고 정보(신고ID, 신고자, 피드ID, 사유)를 REPORT 테이블에 저장합니다.
     */
    int insertReport(@Param("reportId") String reportId,
                     @Param("userId") String userId,
                     @Param("feedId") String feedId,
                     @Param("reportContent") String reportContent);

    /**
     * [피드 작성자 확인]
     * 신고된 피드의 원작자 ID를 조회합니다.
     * 누적 신고에 따른 작성자 제재(Blacklist) 로직을 구현할 때 핵심 데이터로 사용됩니다.
     */
    String selectFeedOwnerUserId(@Param("feedId") String feedId);
}