package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdminBookManagementRepository extends JpaRepository<AdminBooks, Long> {

    @Query("SELECT new com.booknara.booknaraPrj.admin.bookManagement.AdminBookListResponseDto(" +
            "b.bookId, i.isbn13, i.bookTitle, i.authors, i.description, " +
            "i.publisher, i.pubDate, i.naverImage, i.aladinImageBig, " +
            "i.eBookYn, i.epub, g.genreNm, b.bookState) " +
            "FROM AdminBooks b " +
            "LEFT JOIN b.bookIsbn i " +
            "LEFT JOIN i.adminGenre g " +
            "WHERE (:keyword IS NULL OR :keyword = '' " +
            "      OR i.bookTitle LIKE CONCAT(:keyword, '%') " + // 뒤에만 % 붙임
            "      OR i.authors LIKE CONCAT(:keyword, '%') " +   // 뒤에만 % 붙임
            "      OR i.isbn13 = :keyword) " +                  // ISBN은 완전일치 추천
            "AND (:bookState IS NULL OR :bookState = '' OR :bookState = '전체' " +
            "      OR b.bookState = :bookState)")
    Slice<AdminBookListResponseDto> findByFiltersDto(
            @Param("keyword") String keyword,
            @Param("bookState") String bookState,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
// [수정] WHERE 절을 b.bookId 기준으로 변경합니다.
    @Query("UPDATE AdminBooks b SET b.bookState = :bookState WHERE b.bookId = :bookId")
    void updateBookState(@Param("bookId") Long bookId, @Param("bookState") String bookState);
}