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

    @Query("SELECT b.bookId, i.isbn13, i.bookTitle, i.authors, i.description, " +
            "i.publisher, i.pubDate, i.naverImage, i.aladinImageBig, " +
            "i.eBookYn, i.epub, g.genreNm, b.bookState " + // <-- 앞에 SELECT를 반드시 붙여야 합니다.
            "FROM AdminBooks b " +
            "LEFT JOIN b.bookIsbn i " +
            "LEFT JOIN i.adminGenre g " +
            "WHERE (:keyword IS NULL OR i.bookTitle LIKE :keyword OR i.authors LIKE :keyword OR i.isbn13 = :keyword) " +
            "AND (:bookState IS NULL OR :bookState = '' OR b.bookState = :bookState)")
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