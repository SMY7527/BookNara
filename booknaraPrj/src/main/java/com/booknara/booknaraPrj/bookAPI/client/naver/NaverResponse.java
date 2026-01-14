package com.booknara.booknaraPrj.bookAPI.client.naver;

import lombok.Data;
import java.util.List;

/**
 * [NaverResponse]
 * 네이버 도서 검색 API의 최상위 응답 매핑 객체
 */
@Data
public class NaverResponse {

    /**
     * 검색 결과 목록
     * API 응답의 "items" JSON 배열을 NaverDTO 리스트로 변환함
     */
    private List<NaverDTO> items;
}