package com.booknara.booknaraPrj.bookDetail.mapper;

import com.booknara.booknaraPrj.bookDetail.dto.BookDetailDTO;
import com.booknara.booknaraPrj.bookDetail.dto.BookInventoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * [BookDetailMapper]
 * 도서 상세 페이지에 필요한 다양한 도메인의 데이터를 DB에서 조회하는 매퍼입니다.
 * 도서 정보, 장서 관리, 카테고리 계층 정보를 담당합니다.
 */
@Mapper
public interface BookDetailMapper {

    /**
     * [도서 상세 메타 데이터 조회]
     * - BOOK_ISBN 테이블과 GENRE 테이블을 조인하여 도서의 기본 정보를 가져옵니다.
     * @param isbn13 조회할 도서의 고유 식별자
     */
    BookDetailDTO selectBookDetail(@Param("isbn13") String isbn13);

    /**
     * [장서 현황 집계 조회]
     * - BOOKS 테이블에서 해당 ISBN의 총 권수, 대출 가능 권수, 분실 권수를 집계합니다.
     * - SQL 내부에서 COUNT와 CASE WHEN 구문을 활용하여 한 번의 쿼리로 처리하는 것이 효율적입니다.
     */
    BookInventoryDTO selectInventory(@Param("isbn13") String isbn13);

    /**
     * [카테고리 경로 조립용 데이터 조회]
     * - 현재 장르와 바로 위 부모 장르 정보를 한 번에 가져옵니다.
     * - 반환된 Map은 서비스 레이어에서 List<GenreCrumbDTO>로 가공되어 브레드크럼이 됩니다.
     * - @return keys: mall, genreId, genreNm, parentId, parentNm
     */
    Map<String, Object> selectGenreSelfAndParent(@Param("genreId") Integer genreId);

    /**
     * [도서 존재 여부 확인]
     * - 잘못된 ISBN 접근이나 삭제된 도서에 대한 방어 로직으로 사용됩니다.
     */
    int existsByIsbn(@Param("isbn13") String isbn13);

}