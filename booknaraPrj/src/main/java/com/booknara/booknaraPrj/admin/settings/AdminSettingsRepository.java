package com.booknara.booknaraPrj.admin.settings;

import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomBooks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    // 가장 최근(하나뿐인) 설정을 가져오기 위해 상단 1개를 찾는 메서드
    Optional<AdminSettings> findFirstByOrderBySettingsIdAsc();
}