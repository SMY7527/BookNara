package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;

/**
 * [ReviewSaveRequestDTO]
 * 사용자가 작성한 리뷰를 저장(등록 또는 수정)하기 위해 요청하는 데이터 전송 객체입니다.
 * 클라이언트(프론트엔드)에서 작성된 별점과 텍스트 데이터를 백엔드로 전달합니다.
 */
@Data
public class ReviewSaveRequestDTO {

    /** 리뷰를 작성할 대상 도서의 고유 식별자(ISBN13) */
    private String isbn13;

    /** * [별점] 1점에서 5점 사이의 정수값
     * 프론트엔드에서 별 모양 UI를 통해 선택된 값이 바인딩됩니다.
     */
    private Integer rate;

    /** 리뷰의 요약 제목 (FEEDS.FEED_TITLE과 매핑) */
    private String title;

    /** 리뷰의 상세 본문 내용 (FEEDS.CONTENT와 매핑) */
    private String content;
}