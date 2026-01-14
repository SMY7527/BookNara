package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [GenreCrumbDTO]
 * 도서 상세 페이지 상단에 노출되는 브레드크럼(경로 이동)의 단위 정보를 담는 객체입니다.
 * 사용자가 상위 카테고리로 쉽게 이동할 수 있도록 링크 정보를 제공합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreCrumbDTO {

    /** * 장르 고유 식별자
     * 브레드크럼 클릭 시 해당 장르의 도서 목록 페이지로 이동하기 위한 파라미터로 사용됩니다.
     */
    private Integer genreId;

    /** * 장르 명칭
     * 화면에 실제 텍스트로 노출되는 이름입니다. (예: "소설", "경제경영")
     */
    private String genreNm;
}