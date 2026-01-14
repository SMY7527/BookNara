package com.booknara.booknaraPrj.feed.review.dto;

import lombok.Data;
import java.util.List;

/**
 * [ReviewListDTO]
 * 특정 도서에 대한 리뷰 목록과 통계 정보, 페이징 메타데이터를 통합하여 전달하는 객체입니다.
 * 리뷰 섹션의 UI 구성(평점 그래프, 목록, 페이지네이션)에 필요한 모든 데이터를 포함합니다.
 */
@Data
public class ReviewListDTO {
    /** 조회 대상 도서의 고유 식별자(ISBN13) */
    private String isbn13;

    /** [통계 정보] 평균 별점 및 총 리뷰 개수 요약 */
    private ReviewSummaryDTO summary;

    /** [데이터 목록] 현재 페이지에 해당하는 리뷰 상세 항목 리스트 */
    private List<ReviewItemDTO> items;

    // --- [페이징 메타데이터] ---
    /** 현재 조회된 페이지 번호 (1부터 시작) */
    private int page;

    /** 한 페이지당 보여줄 리뷰 개수 (보통 5개 또는 10개) */
    private int size;

    /** * [전체 리뷰 수]
     * DB의 COUNT(*) 결과값으로, 프론트엔드에서 전체 페이지 수를 계산하는 기준이 됩니다.
     */
    private long total;
}