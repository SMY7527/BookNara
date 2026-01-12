package com.booknara.booknaraPrj.admin.Event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventsRepository extends JpaRepository<AdminEvent, Long> {

    // [추가] 종료일(endAt)이 현재 시간보다 이후인 모든 이벤트 조회
    // 쿼리 메서드 방식: findAllByEndAtAfter
    List<AdminEvent> findAllByEndAtAfter(LocalDateTime now);

    // 기존 메서드들 유지
    List<AdminEvent> findByEventMainYn(String eventMainYn);

    @Modifying
    @Transactional
    @Query(value = "UPDATE EVENTS SET EVENT_MAIN_YN = 'N'", nativeQuery = true)
    void resetAllMainBanner();

    @Modifying
    @Transactional
    @Query(value = "UPDATE EVENTS SET EVENT_MAIN_YN = 'Y' WHERE EVENT_ID IN :ids", nativeQuery = true)
    void updateMainBannerStatus(@Param("ids") List<Long> ids);
}
