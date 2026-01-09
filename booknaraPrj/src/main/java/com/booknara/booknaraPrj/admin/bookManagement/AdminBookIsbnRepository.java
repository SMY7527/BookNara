package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminBookIsbnRepository extends JpaRepository<AdminBookIsbn, String> {
    // 기본 save, findById 등을 제공하므로 추가 메서드 없이도 등록이 가능합니다.

    // 1. 수동 검색: 제목 또는 ISBN으로 페이징 조회
    Page<AdminBookIsbn> findByBookTitleContainingOrIsbn13Containing(String title, String isbn, Pageable pageable);

    // 2. 랜덤 추출: MySQL RAND() 함수 사용 (성능을 위해 LIMIT 제한)
    @Query(value = "SELECT * FROM BOOK_ISBN WHERE EBOOK_YN = 'Y' ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<AdminBookIsbn> findRandomBooks(@Param("count") int count);
}