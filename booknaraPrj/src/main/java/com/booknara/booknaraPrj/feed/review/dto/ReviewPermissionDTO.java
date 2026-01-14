package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

/**
 * [ReviewPermissionDTO]
 * 특정 도서에 대해 현재 사용자가 리뷰를 작성하거나 수정할 권한이 있는지 확인하는 DTO입니다.
 * 도서관의 대출/반납 정책과 커뮤니티 정책을 결합하여 권한을 판별합니다.
 */
@Data
public class ReviewPermissionDTO {
    /** 권한을 확인할 대상 도서의 고유 식별자(ISBN13) */
    private String isbn13;

    /** * [리뷰 작성 자격 여부]
     * - 'Y': 대출 후 정상적으로 반납한 이력이 있어 리뷰 작성이 가능함
     * - 'N': 이용 이력이 없거나 현재 대출 중인 상태라 작성이 제한됨
     * - 효과: 허위 리뷰나 무분별한 광고성 리뷰를 방지하는 필터 역할을 합니다.
     */
    private String allowedYn;

    /** * [기존 리뷰 존재 여부]
     * - 'Y': 이미 이 책에 대해 작성한 리뷰가 있음 (작성 버튼 대신 '수정' 버튼 노출)
     * - 'N': 아직 작성한 리뷰가 없음
     */
    private String hasReviewYn;

    /** * [내가 쓴 리뷰 ID]
     * - hasReviewYn이 'Y'일 때, 수정 페이지로 이동하기 위한 해당 피드의 고유 ID입니다.
     */
    private String myFeedId;

    /** * [사용자 안내 메시지]
     * - 권한이 없을 경우 "반납 후 리뷰를 남겨주세요" 등 사유를 UI에 즉시 노출하기 위한 문구입니다.
     */
    private String message;
}