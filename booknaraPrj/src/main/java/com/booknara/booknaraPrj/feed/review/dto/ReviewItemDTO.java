package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * [ReviewItemDTO]
 * 피드 화면이나 도서 상세 페이지의 리뷰 목록에서 개별 리뷰 항목을 표시하기 위한 DTO입니다.
 * 리뷰 내용과 작성자의 프로필 정보를 결합하여 소셜 피드 기능을 지원합니다.
 */
@Data
public class ReviewItemDTO {
    /** 리뷰가 속한 피드의 고유 ID (수정/삭제/상세 이동 시 사용) */
    private String feedId;

    /** 리뷰 대상 도서의 ISBN13 */
    private String isbn13;

    // --- [작성자 프로필 정보] ---
    /** 작성자 계정 ID */
    private String userId;
    /** 작성자의 별명(프로필명) */
    private String profileNm;
    /** 작성자의 프로필 이미지 경로 */
    private String profileImg;
    /** 프로필 이미지 사용 여부 플래그 (0: 기본이미지, 1: 사용자설정이미지) */
    private Integer useImg;

    /** [별점] 1점~5점 사이의 정수 값 */
    private Integer rate;

    /** 리뷰 제목 (FEEDS 테이블 연동) */
    private String title;
    /** 리뷰 본문 내용 */
    private String content;

    /** 리뷰 작성 일시 */
    private LocalDateTime createdAt;

    /** * [본인 글 여부 확인]
     * 현재 접속자와 작성자가 일치하면 "Y", 아니면 "N"
     * UI에서 [수정], [삭제] 버튼의 노출 여부를 결정하는 핵심 데이터입니다.
     */
    private String mineYn;
}