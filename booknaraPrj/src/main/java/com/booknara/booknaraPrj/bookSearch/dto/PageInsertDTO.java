package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

/**
 * [PageInsertDTO]
 * 도서 검색 결과의 페이징 처리를 위한 데이터를 담는 DTO입니다.
 * 프론트엔드에서 요청한 페이지 정보와 DB의 LIMIT/OFFSET 쿼리를 연결하는 교량 역할을 합니다.
 */
@Data
public class PageInsertDTO {

    /** * 현재 요청된 페이지 번호
     * (일반적으로 사용자가 보는 UI에서는 1부터 시작합니다.)
     */
    private int page;

    /** * 한 페이지에 보여줄 도서 리스트의 개수
     * (예: 10건, 20건 등 서비스 정책에 따라 설정됩니다.)
     */
    private int size;

    /** * 데이터베이스(MySQL 등)에서 조회를 시작할 행의 위치
     * SQL의 'LIMIT #{offset}, #{size}' 구문에서 사용됩니다.
     */
    private int offset;
}