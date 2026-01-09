package com.booknara.booknaraPrj.admin.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, String> {
    // 검색 기능을 위한 메소드 (이름 또는 이메일에 키워드 포함 시)
    List<Users> findByUserNmContainingOrEmailContaining(String nameKeyword, String emailKeyword);
    List<Users> findByUserIdContainingOrUserNmContainingOrEmailContaining(String userId, String userNm, String email);
    // 상태별 인원수 카운트 (통계용)
    long countByUserState(String userState);
}
