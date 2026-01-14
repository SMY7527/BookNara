package com.booknara.booknaraPrj.bookAPI.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * [BookIsbnTempDTO]
 * 외부 API 수집 데이터를 통합 관리하는 Staging DTO
 * BOOK_ISBN_TEMP 테이블과 1:1 매핑됨
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookIsbnTempDTO {

    private String isbn13;                  // ISBN13 (PK)

    // --- 핵심 도서 정보 ---
    private String bookTitle;               // 도서명
    private String authors;                 // 저자명 (네이버/알라딘 데이터 융합)
    private String publisher;               // 출판사
    private Integer genreId;                // 장르 ID (알라딘 카테고리 기반 매핑)

    // --- 상세 및 이미지 정보 ---
    private String description;             // 도서 요약 설명
    private String pubdate;                 // 출판일 (YYYYMMDD)
    private String naverImage;              // 네이버 수집 이미지 URL
    private String aladinImageBig;          // 알라딘 수집 고화질 이미지 URL

    // --- 수집 제어 및 상태 정보 ---
    private String dataHash;                // 변경 감지용 해시값 (업데이트 여부 판단)

    private LocalDateTime infonaruFetchedAt; // 정보나루 수집 시각
    private LocalDateTime naverFetchedAt;    // 네이버 수집 시각
    private LocalDateTime aladinFetchedAt;   // 알라딘 수집 시각

    /** 네이버 응답 상태 (0:미시도, 1:성공(데이터 있음), 2:성공(데이터 없음), 3:재시도가능실패, 4:불가실패) */
    private Integer naverResStatus;
    /** 알라딘 응답 상태 (0:미시도, 1:성공(데이터 있음), 2:성공(데이터 없음), 3:재시도가능실패, 4:불가실패) */
    private Integer aladinResStatus;

    /** 데이터 처리 상태 (0:NOTREADY, 1:READY, 2:MERGED) */
    private Integer statusCd;
}