package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookSearchConditionDTO {
    // 검색
    private String keyword;      // LIKE용(원문 trim, 공백 축소)
    private String keywordNs;    // LIKE용(공백 제거 버전)
    private String ftKeyword;    // FULLTEXT용(+토큰*)
    private String ftJoined;     // FULLTEXT용(토큰을 붙인 +치킨* 같은 버전)


    private String field;     // TITLE / AUTHOR / PUBLISHER / ALL
    private String mall;  // 국내도서 / 외국도서

    // 카테고리
    private Integer genreId;         // 소분류
    private Integer parentGenreId;   // 대분류

    // 필터
    private String ebookYn;    // ALL / Y / N

    // 정렬
    private String sort;       // NEW / RATING / REVIEW
    
    //풀 컨텍스트 대상 판정
    private Boolean useFulltext; // 또는 boolean useFulltext;

    //외국도서 카테고리
    private List<Integer> foreignTopParentIds = new ArrayList<>();

}

