package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdminBookManagementRepository extends JpaRepository<AdminBooks, Long> {

    @Query(value = "SELECT new com.booknara.booknaraPrj.admin.bookManagement.AdminBookListResponseDto(" +
            "i.isbn13, i.bookTitle, i.authors, i.description, i.publisher, i.pubDate, " +
            "i.naverImage, i.aladinImageBig, i.eBookYn, i.epub, g.genreNm, b.bookState) " +
            "FROM AdminBooks b " +
            "JOIN b.bookIsbn i " +
            "JOIN i.adminGenre g " + // i.genre를 i.adminGenre로 수정!
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT count(b) FROM AdminBooks b")
    Page<AdminBookListResponseDto> findAllBookManagementList(Pageable pageable);

    // 필터 검색 쿼리도 동일하게 수정
    @Query(value = "SELECT new com.booknara.booknaraPrj.admin.bookManagement.AdminBookListResponseDto(" +
            "i.isbn13, i.bookTitle, i.authors, i.description, i.publisher, i.pubDate, " +
            "i.naverImage, i.aladinImageBig, i.eBookYn, i.epub, g.genreNm, b.bookState) " +
            "FROM AdminBooks b " +
            "JOIN b.bookIsbn i " +
            "JOIN i.adminGenre g " + // 여기도 i.adminGenre로 수정!
            "WHERE (:keyword IS NULL OR i.bookTitle LIKE %:keyword% OR i.authors LIKE %:keyword% OR i.isbn13 LIKE %:keyword%) " +
            "AND (:bookState IS NULL OR :bookState = '' OR b.bookState = :bookState) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT count(b) FROM AdminBooks b JOIN b.bookIsbn i WHERE (:keyword IS NULL OR i.bookTitle LIKE %:keyword%)")
    Page<AdminBookListResponseDto> findByFilters(
            @Param("keyword") String keyword,
            @Param("bookState") String bookState,
            Pageable pageable);

    @Modifying
    @Transactional
    // 3. Books -> AdminBooks로 수정
    @Query("UPDATE AdminBooks b SET b.bookState = :bookState WHERE b.bookIsbn.isbn13 = :isbn13")
    void updateBookState(@Param("isbn13") String isbn13, @Param("bookState") String bookState);
}