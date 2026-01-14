package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * [GenrePathDTO]
 * 도서 상세 페이지에서 해당 도서가 속한 전체 카테고리 경로를 관리하는 객체입니다.
 * '몰(Mall) 구분'과 '계층형 브레드크럼(Breadcrumb)' 정보를 통합하여 전달합니다.
 */
@Data
public class GenrePathDTO {

    /** * [몰 구분]
     * 도서의 대분류 영역을 정의합니다.
     * (예: 국내도서, 외국도서, 전자책, 음반, DVD 등)
     */
    private String mall;

    /** * [계층형 경로 목록]
     * 대분류부터 현재 도서가 속한 최하위 소분류까지의 순서대로 정렬된 리스트입니다.
     * 예: [ {id:1, nm:"소설"}, {id:2, nm:"영미소설"}, {id:3, nm:"추리/미스터리"} ]
     */
    private List<GenreCrumbDTO> crumbs = new ArrayList<>();
}