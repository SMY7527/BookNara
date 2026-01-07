package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

@Data
public class BookSearchDTO {
    //도서정보
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;
    private String pubdate;//VARCHAR(8) (예: 20160502)
    private String description;
    private String naverImage;
    private String aladinImageBig;
    private String ebookYn;   // 'Y'/'N'
    //장르정보
    private Integer genreId;
    private String genreNm;
    //평점
    private Double ratingAvg;
    private Integer reviewCnt;
    // 대여_예약 현황
    private Integer ownedCnt;      // BOOKS(STATE='N') 권수
    private Integer lendingCnt;    // 현재 대여중(RETURN_DONE_AT IS NULL) 권수
    private Integer availableCnt;  // max(ownedCnt - lendingCnt, 0)
    private Integer rsvActiveCnt;  // RSV_STATUS='ACTIVE' 예약 수

    // 내 상태
    private String myLendYn;       // 내 대여중 여부
    private String myExtendYn;     // 연장 가능 여부(반납 7일전 + EXTEND_CNT=0)
    private String myLendId;       // 연장 요청용 LEND_ID

    private String myRsvYn;        // 내 ACTIVE 예약 여부
    private String myRsvId;        // 예약취소 요청용 RSV_ID

    // 예약 불가 조건용
    private String userBlockedYn;  // 정지/탈퇴 상태(USER_STATE 3/4)
    private String userOverdueYn;  // 연체중(활성대여 & OVER_DUE='Y')
    private String rsvLimitYn;     // 도서별 ACTIVE 예약 >= 10

}
