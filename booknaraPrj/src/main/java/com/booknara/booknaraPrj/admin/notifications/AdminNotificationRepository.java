package com.booknara.booknaraPrj.admin.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    // 1. 읽지 않은 알림 개수 조회 (사이드바 배지용)
    long countByCheckYn(String checkYn);

    /**
     * [성능 개선 및 기능 통합 쿼리]
     * - 5개 테이블 조인: 알림 + 사용자 + 대여 + 도서 + 도서상세(ISBN)
     * - DTO 직접 조회: 메모리 효율 극대화
     * - 동적 필터링: 타입(대여/반납/연체) 및 검색어(회원명/ID/대여ID/도서제목) 통합
     */
    @Query("SELECT new com.booknara.booknaraPrj.admin.notifications.AdminNotiResponseDto(" +
            "AN.NOTI_ID, AN.NOTI_TYPE, " +
            "CONCAT(U.USER_NM, '님이 [', COALESCE(BI.BOOK_TITLE, '알 수 없는 도서'), '] 도서를 ', " +
            "CASE WHEN AN.NOTI_TYPE = '연체' THEN '연체 중입니다.' " +
            "     WHEN AN.NOTI_TYPE = '대여' THEN '대여 신청하였습니다.' " +
            "     ELSE '반납 완료하였습니다.' END), " +
            "U.USER_ID, U.USER_NM, L.LEND_ID, BI.BOOK_TITLE, " +
            "BI.ISBN13, " +
            "AN.CREATED_AT, AN.UPDATED_AT) " +
            "FROM ADMIN_NOTIFICATION AN " +
            "JOIN USERS U ON AN.USER_ID = U.USER_ID " +
            "LEFT JOIN LEND L ON AN.LEND_ID = L.LEND_ID " +
            "LEFT JOIN ADMIN_BOOKS B ON L.BOOK_ID = B.BOOK_ID " +
            "LEFT JOIN ADMIN_BOOK_ISBN BI ON B.BOOK_ISBN.ISBN13 = BI.ISBN13 " +
            "WHERE (:type IS NULL OR :type = '전체' OR AN.NOTI_TYPE = :type) " +
            "AND (:keyword IS NULL OR U.USER_NM LIKE :keyword OR U.USER_ID LIKE :keyword OR CAST(L.LEND_ID AS STRING) LIKE :keyword OR BI.BOOK_TITLE LIKE :keyword)")
    Page<AdminNotiResponseDto> findDetailedNotifications(
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ADMIN_NOTIFICATION N SET N.CHECK_YN = 'Y', N.UPDATED_AT = :now WHERE N.CHECK_YN = 'N'")
    int markAllAsRead(@Param("now") LocalDateTime now);
}