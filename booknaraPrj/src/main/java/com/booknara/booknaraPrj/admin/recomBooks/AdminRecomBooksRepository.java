package com.booknara.booknaraPrj.admin.recomBooks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AdminRecomBooksRepository extends JpaRepository<AdminRecomBooks, Integer> {
    // 기존 추천 목록 일괄 삭제 (업데이트 시 교체 방식)
// 기존 추천 목록을 모두 비활성화(inactive) 상태로 변경
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트 초기화
    @Query("UPDATE AdminRecomBooks r SET r.state = 'inactive' WHERE r.state = 'active'")
    void deactivateAllActive();
}