package com.booknara.booknaraPrj.admin.recomBooks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRecomBooksRepository extends JpaRepository<AdminRecomBooks, Long> {

    // 1. 현재 활성화(active) 상태인 추천 도서 목록 조회
    List<AdminRecomBooks> findByState(AdminRecomState state);

    // 2. ISBN13으로 기존 추천 도서 존재 여부 확인 (수정 중 에러 나던 부분)
    Optional<AdminRecomBooks> findByIsbn13(String isbn13);

    // 3. 존재 여부만 빠르게 확인하고 싶을 때 (선택 사항)
    boolean existsByIsbn13(String isbn13);
}