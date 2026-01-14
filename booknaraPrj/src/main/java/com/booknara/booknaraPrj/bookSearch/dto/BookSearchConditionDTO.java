package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * [BookSearchConditionDTO]
 * 검색 필터, 정렬, 카테고리 선택 등 복합적인 검색 조건을 담는 데이터 전송 객체입니다.
 * 특히 하나의 키워드를 LIKE용과 FULLTEXT용으로 구분하여 처리하는 핵심 파라미터 셋입니다.
 */
@Data
public class BookSearchConditionDTO {

    // --- [1] 검색 키워드 관련 (Multi-faceted Search) ---

    /** 일반 LIKE 검색용: 원문에서 양 끝 공백 제거 및 연속된 공백을 단일화한 버전 */
    private String keyword;

    /** 일반 LIKE 검색용(공백 제거): "해리 포터"를 "해리포터"로 검색해도 나오게 하기 위한 버전 */
    private String keywordNs;

    /** FULLTEXT 검색용: 단어별로 '+토큰*' 형태의 Boolean Mode 연산자가 포함된 버전 */
    private String ftKeyword;

    /** FULLTEXT 검색용(결합): 여러 검색어를 조합하여 전문 검색 인덱스를 타게 하는 버전 */
    private String ftJoined;


    /** 검색 대상 필드: TITLE(제목), AUTHOR(저자), PUBLISHER(출판사), ALL(전체) */
    private String field;

    /** 몰 구분: 국내도서 / 외국도서 / ALL */
    private String mall;

    // --- [2] 카테고리 계층 구조 ---

    /** 소분류 장르 ID: 특정 카테고리를 직접 선택했을 때 사용 */
    private Integer genreId;

    /** 대분류 장르 ID: 상위 카테고리 내의 모든 도서를 조회할 때 사용 */
    private Integer parentGenreId;

    // --- [3] 데이터 필터링 ---

    /** eBook 여부 필터: ALL(전체), Y(eBook만), N(종이책만) */
    private String ebookYn;

    // --- [4] 결과 정렬 전략 ---

    /** 정렬 기준: NEW(신간순), RATING(평점순), REVIEW(리뷰많은순) */
    private String sort;

    /** 전문 검색(Full-text) 엔진 사용 여부 판정 플래그
     * true일 경우 DB 매퍼에서 MATCH() AGAINST() 구문을 활성화합니다.
     */
    private Boolean useFulltext;

    // --- [5] 외국도서 전용 로직 ---

    /** 외국도서 최상위 카테고리 ID 리스트
     * 국내도서와 계층 구조가 다른 외국도서의 특수 필터링을 위해 사용됩니다.
     */
    private List<Integer> foreignTopParentIds = new ArrayList<>();

}