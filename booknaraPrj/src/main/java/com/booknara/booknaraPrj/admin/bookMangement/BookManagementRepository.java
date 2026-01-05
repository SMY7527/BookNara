// BookManagementRepository.java
package com.booknara.booknaraPrj.admin.bookMangement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookManagementRepository extends JpaRepository<Books, Long> {

    @Query("SELECT new com.booknara.booknaraPrj.admin.bookMangement.BookListResponseDto(" +
            "i.isbn13, i.bookTitle, i.authors, i.description, i.publisher, i.pubDate, " +
            "i.aladinImageBig, i.eBookYn, i.epub, g.genreNm, b.bookState) " +
            "FROM Books b " +
            "JOIN b.bookIsbn i " +
            "JOIN i.genre g " +
            "ORDER BY b.createdAt DESC")
    Page<BookListResponseDto> findAllBookManagementList(Pageable pageable);

    @Query("SELECT new com.booknara.booknaraPrj.admin.bookMangement.BookListResponseDto(" +
            "i.isbn13, i.bookTitle, i.authors, i.description, i.publisher, i.pubDate, " +
            "i.aladinImageBig, i.eBookYn, i.epub, g.genreNm, b.bookState) " +
            "FROM Books b JOIN b.bookIsbn i JOIN i.genre g " +
            "WHERE i.bookTitle LIKE %:keyword% OR i.authors LIKE %:keyword% OR i.isbn13 LIKE %:keyword% " +
            "ORDER BY b.createdAt DESC")
    Page<BookListResponseDto> findByKeyword(String keyword, Pageable pageable);
}