package com.booknara.booknaraPrj.bookDetail.mapper;

import com.booknara.booknaraPrj.bookDetail.dto.BookDetailDTO;
import com.booknara.booknaraPrj.bookDetail.dto.BookInventoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface BookDetailMapper {

    //도서 상세 메타 조회 (BOOK_ISBN + GENRE(현재))

    BookDetailDTO selectBookDetail(@Param("isbn13") String isbn13);

    //ISBN 기준 소장/가용/분실 집계 (BOOKS)
    BookInventoryDTO selectInventory(@Param("isbn13") String isbn13);

    /**
     * breadcrumb 구성용: 현재 장르 + 부모 장르(1단) 조회
     * - crumbs는 서비스에서 조립
     * - 반환 Map keys: mall, genreId, genreNm, parentId, parentNm
     */
    Map<String, Object> selectGenreSelfAndParent(@Param("genreId") Integer genreId);
    int existsByIsbn(@Param("isbn13") String isbn13);

}
