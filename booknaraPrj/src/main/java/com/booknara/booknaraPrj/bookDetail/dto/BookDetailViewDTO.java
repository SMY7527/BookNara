package com.booknara.booknaraPrj.bookDetail.dto;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import lombok.Data;

import java.util.List;

/**
 * [BookDetailViewDTO]
 * 도서 상세 화면(Detail Page) 전체를 렌더링하기 위한 통합 뷰 모델입니다.
 * 관련 도메인의 모든 정보를 취합하여 클라이언트에게 단일 응답으로 전달합니다.
 */
@Data
public class BookDetailViewDTO {
    /** [기본 정보] 도서명, 저자, 출판사, 이미지 URL 등 */
    private BookDetailDTO bookDetailDTO;

    /** [카테고리 경로] 상위 장르부터 현재 장르까지의 내비게이션 정보 (Breadcrumb용) */
    private GenrePathDTO genrePath;

    /** [실시간 상태] 대출 가능 권수, 예약 현황, 내 대출 상태 등 */
    private BookInventoryDTO inventory;

    // --- [형제 도서 연결: 종이책 <-> 전자책] ---
    /** 현재 도서와 쌍을 이루는 도서의 ISBN (예: 종이책 상세에서 전자책으로 이동 시 사용) */
    private String pairIsbn13;
    /** 이동 버튼에 표시할 문구 (예: "전자책 보기", "종이책 보기") */
    private String pairLabel;
    /** 쌍이 되는 도서가 존재하는지 여부 ('Y'/'N') */
    private String pairYn;

    // --- 리뷰 데이터 ---
    /** 평균 별점 및 총 리뷰 개수 통계 */
    private ReviewSummaryDTO reviewSummary;
    /** 상세 페이지 하단에 미리 보여줄 최신 리뷰 리스트 */
    private List<ReviewItemDTO> reviewPreview;

    // --- 북마크 ---
    /** 현재 로그인한 사용자가 이 책을 북마크했는지 여부 ('Y'/'N') */
    private String bookmarkedYn;
    /** 이 도서가 받은 총 북마크(관심) 수 */
    private int bookmarkCnt;
}