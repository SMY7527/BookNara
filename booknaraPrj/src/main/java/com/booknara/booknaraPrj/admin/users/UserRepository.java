package com.booknara.booknaraPrj.admin.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, String> {

    // ✅ 검색 결과도 페이징 처리 (Pageable 추가)
    @Query("SELECT u FROM Users u WHERE " +
            "u.userId LIKE %:kw% OR " +
            "u.userNm LIKE %:kw% OR " +
            "u.email LIKE %:kw%")
    Page<Users> searchUsers(@Param("kw") String keyword, Pageable pageable);

    // ✅ 추가: 상태값(1, 2, 3)으로만 검색할 때 사용
    Page<Users> findByUserState(String userState, Pageable pageable);
    Page<Users> findByUserStateNot(String userState, Pageable pageable);
    // 상태별 인원수 카운트
    long countByUserState(String userState);

    // 단일 조건 검색 (이미 Page로 잘 만드셨습니다)
    Page<Users> findByUserIdContaining(String keyword, Pageable pageable);
}
