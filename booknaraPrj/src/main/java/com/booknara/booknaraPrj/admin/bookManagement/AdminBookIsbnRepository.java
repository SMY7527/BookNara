package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AdminBookIsbnRepository extends JpaRepository<AdminBookIsbn, String> {

    /**
     * 1. 랜덤 추출용: 종이책(N) 전체 개수 조회
     * 서비스에서 파라미터 없이 호출하므로 파라미터를 제거한 버전입니다.
     */
    @Query(value = "SELECT COUNT(*) FROM BOOK_ISBN WHERE EBOOK_YN = 'N'", nativeQuery = true)
    long countByEBookYnNative();

    /**
     * 2. 랜덤 추출용: 직접 계산한 LIMIT와 OFFSET으로 데이터 조회
     * 서비스의 findRandomBooksWithOffset 호출에 대응합니다.
     */
    @Query(value = "SELECT * FROM BOOK_ISBN WHERE EBOOK_YN = 'N' LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<AdminBookIsbn> findRandomBooksWithOffset(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 3. 저장 로직용: ISBN으로 도서 상세 정보 한 건 조회
     */
    Optional<AdminBookIsbn> findByIsbn13(String isbn13);

    /**
     * 4. 기존 수동 검색용: 제목 또는 ISBN 검색
     */
    Page<AdminBookIsbn> findByBookTitleContainingOrIsbn13Containing(String title, String isbn, Pageable pageable);

    // [참고] 만약 Pageable을 사용하는 버전이 필요하다면 아래 메서드도 유지하세요.
    @Query(value = "SELECT * FROM BOOK_ISBN WHERE EBOOK_YN = 'N'",
            countQuery = "SELECT COUNT(*) FROM BOOK_ISBN WHERE EBOOK_YN = 'N'",
            nativeQuery = true)
    Page<AdminBookIsbn> findAllByEBookYnNative(Pageable pageable);
}