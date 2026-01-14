package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

/**
 * [BookSearchDTO]
 * 도서 검색 결과 및 상세 정보 조회를 위한 통합 데이터 전송 객체입니다.
 * 도서 정보, 장르, 평점, 그리고 사용자별 맞춤형 상태 플래그를 포함합니다.
 */
@Data
public class BookSearchDTO {

    // --- [1] 도서 기본 정보 (Core Metadata) ---
    private String isbn13;           // 도서 고유 식별자 (13자리)
    private String bookTitle;        // 도서 제목
    private String authors;          // 저자명
    private String publisher;        // 출판사
    private String pubdate;          // 출간일 (VARCHAR 8자리, 예: 20160502)
    private String description;      // 도서 상세 설명
    private String naverImage;       // 네이버 제공 표지 이미지 URL
    private String aladinImageBig;   // 알라딘 제공 고화질 표지 이미지 URL
    private String ebookYn;          // 전자책 여부 ('Y'/'N')

    // --- [2] 장르 카테고리 정보 ---
    private Integer genreId;         // 장르 고유 ID
    private String genreNm;          // 장르명 (예: 소설, 기술과학 등)

    // --- [3] 사용자 반응 데이터 (Social Metrics) ---
    private Double ratingAvg;        // 평균 평점
    private Integer reviewCnt;       // 등록된 리뷰 총수

    // --- [4] 개인화 관심 데이터 (Interaction Flags) ---
    private String bookmarkedYn;     // 현재 사용자의 북마크 여부 ('Y'/'N')

    /** 내 장바구니 담김 여부 ('Y'/'N') */
    private String myCartYn;
    /** 장바구니에 담긴 경우, 삭제 처리를 위한 고유 식별자 */
    private Long myCartId;

    // --- [5] 도서 유동 현황 (Inventory & Circulation) ---
    /** 도서관 보유 총 권수 (정상 상태 도서 기준) */
    private Integer ownedCnt;
    /** 현재 대출 중인 권수 (아직 반납되지 않은 상태) */
    private Integer lendingCnt;
    /** 즉시 대출 가능 권수 (ownedCnt - lendingCnt) */
    private Integer availableCnt;
    /** 현재 예약 대기 중인 인원수 */
    private Integer rsvActiveCnt;

    // --- [6] 내 대출/예약 상세 상태 (User's Current Status) ---
    private String myLendYn;         // 내가 현재 이 도서를 대출 중인지 여부
    private String myExtendYn;       // 반납 연장 가능 여부 (반납 7일 전부터 + 기존 연장 이력 없을 때만 'Y')
    private String myLendId;         // 연장 신청 시 필요한 대출 고유 ID

    private String myRsvYn;          // 내가 현재 이 도서를 예약 중인지 여부
    private String myRsvId;          // 예약 취소 시 필요한 예약 고유 ID

    // --- [7] 서비스 제어 플래그 (Business Rule Flags) ---
    /** 사용자 계정 상태에 따른 대출/예약 차단 여부 (정지/탈퇴 등) */
    private String userBlockedYn;
    /** 사용자 연체 여부에 따른 차단 여부 (현재 연체 중인 도서가 있을 때) */
    private String userOverdueYn;
    /** 도서별 예약 한도 초과 여부 (현재 예약자가 10명 이상일 때 'Y') */
    private String rsvLimitYn;

}