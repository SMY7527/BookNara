package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * [InfoNaruPageResult]
 * 정보나루 API 검색 결과의 페이징 정보와 가공된 데이터 리스트를 담는 객체
 */
@Getter
@AllArgsConstructor
public class InfoNaruPageResult {
    private final int pageNo;    // 현재 응답받은 페이지 번호
    private final int pageSize;  // 페이지당 포함된 데이터 개수
    private final int numFound;  // 검색 조건에 해당하는 전체 도서 건수 (총 루프 횟수 계산용)

    /** * 외부 DTO에서 시스템 내부 Staging 규격(BookIsbnTempDTO)으로
     * 1차 변환이 완료된 도서 목록
     */
    private final List<BookIsbnTempDTO> books;
}