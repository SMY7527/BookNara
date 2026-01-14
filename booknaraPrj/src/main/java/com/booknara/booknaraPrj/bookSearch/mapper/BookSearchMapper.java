package com.booknara.booknaraPrj.bookSearch.mapper;

import com.booknara.booknaraPrj.bookSearch.dto.BookSearchConditionDTO;
import com.booknara.booknaraPrj.bookSearch.dto.BookSearchDTO;
import com.booknara.booknaraPrj.bookSearch.dto.PageInsertDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * [BookSearchMapper]
 * 도서 검색 엔진의 데이터 접근 인터페이스입니다.
 * XML 매퍼 파일의 각 쿼리 ID와 연결되어 실제 DB 조회를 수행합니다.
 */
@Mapper
public interface BookSearchMapper {

    /**
     * [도서 목록 검색]
     * 검색 키워드, 필터링, 정렬 기준을 적용하여 실제 도서 데이터 목록을 가져옵니다.
     * * @param cond   검색어(Full-text/LIKE), 카테고리, 정렬 등 비즈니스 검색 조건
     * @param page   LIMIT/OFFSET 계산 정보가 포함된 페이징 객체
     * @param userId 북마크, 내 장바구니, 대출 상태 등 개인화 정보를 조회하기 위한 사용자 ID
     * @return 검색 결과에 부합하는 도서 DTO 리스트
     */
    List<BookSearchDTO> searchBooks(
            @Param("cond") BookSearchConditionDTO cond,
            @Param("page") PageInsertDTO page,
            @Param("userId") String userId
    );

    /**
     * [검색 결과 전체 건수 조회]
     * 페이징 바(Pagination Bar) 구성을 위해 검색 조건에 맞는 전체 도서 수량을 조회합니다.
     * * @param cond   검색 조건 (목록 조회와 동일한 조건 적용 필수)
     * @param userId 사용자별 접근 권한이나 상태에 따른 필터링이 필요할 경우 활용
     * @return 검색된 총 도서 건수
     */
    long countBooks(
            @Param("cond") BookSearchConditionDTO cond,
            @Param("userId") String userId
    );
}