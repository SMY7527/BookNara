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
            "an.notiId, an.notiType, " +
            "CONCAT(u.userNm, '님이 [', COALESCE(bi.bookTitle, '알 수 없는 도서'), '] 도서를 ', " +
            "CASE WHEN an.notiType = '연체' THEN '연체 중입니다.' " +
            "     WHEN an.notiType = '대여' THEN '대여 신청하였습니다.' " +
            "     ELSE '반납 완료하였습니다.' END), " +
            "u.userId, u.userNm, l.lendId, bi.bookTitle, " +
            "bi.isbn13, " + // <--- 여기도 b.isbn13 대신 bi.isbn13 사용 권장
            "an.createdAt, an.updatedAt) " +
            "FROM AdminNotification an " +
            "JOIN Users u ON an.userId = u.userId " +
            "LEFT JOIN Lend l ON an.lendId = l.lendId " +
            "LEFT JOIN AdminBooks b ON l.bookId = b.bookId " +
            "LEFT JOIN AdminBookIsbn bi ON b.bookIsbn.isbn13 = bi.isbn13 " + // <--- [핵심 수정!] b.bookIsbn.isbn13으로 변경
            "WHERE (:type IS NULL OR :type = '전체' OR an.notiType = :type) " +
            "AND (:keyword IS NULL OR u.userNm LIKE :keyword OR u.userId LIKE :keyword OR CAST(l.lendId AS string) LIKE :keyword OR bi.bookTitle LIKE :keyword)")
    Page<AdminNotiResponseDto> findDetailedNotifications(
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AdminNotification n SET n.checkYn = 'Y', n.updatedAt = :now WHERE n.checkYn = 'N'")
    int markAllAsRead(@Param("now") LocalDateTime now);
}