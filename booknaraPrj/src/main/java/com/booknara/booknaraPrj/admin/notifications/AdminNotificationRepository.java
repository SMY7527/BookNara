/*
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

    */
/**
     * [성능 개선 및 기능 통합 쿼리]
     * - 5개 테이블 조인: 알림 + 사용자 + 대여 + 도서 + 도서상세(ISBN)
     * - DTO 직접 조회: 메모리 효율 극대화
     * - 동적 필터링: 타입(대여/반납/연체) 및 검색어(회원명/ID/대여ID/도서제목) 통합
     *//*

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
}*/

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

    // 1. 읽지 않은 알림 개수 조회 (변수명 checkYn 확인 필수)
    long countByCheckYn(String checkYn);

    /**
     * [수정 완료]
     * 1. 테이블명 -> 엔티티명 (AdminNotification, Users, Lend, AdminBooks, AdminBookIsbn)
     * 2. 컬럼명 -> 필드명 (notiId, notiType, userId, userNm, lendId, bookTitle 등)
     * 3. LIKE 검색 시 % 위치 주의
     */
    @Query("SELECT new com.booknara.booknaraPrj.admin.notifications.AdminNotiResponseDto(" +
            "an.notiId, an.notiType, " +
            "CONCAT(u.userNm, '님이 [', COALESCE(bi.bookTitle, '알 수 없는 도서'), '] 도서를 ', " +
            "CASE WHEN an.notiType = '연체' THEN '연체 중입니다.' " +
            "     WHEN an.notiType = '대여' THEN '대여 신청하였습니다.' " +
            "     ELSE '반납 완료하였습니다.' END), " +
            "u.userId, u.userNm, l.lendId, bi.bookTitle, " +
            "bi.isbn13, " +
            "an.createdAt, an.updatedAt) " +
            "FROM AdminNotification an " +             // ADMIN_NOTIFICATION -> AdminNotification
            "JOIN Users u ON an.userId = u.userId " +  // USERS -> Users (관계 매핑이 안되어 있다면 ON절 사용)
            "LEFT JOIN Lend l ON an.lendId = l.lendId " + // LEND -> Lend
            "LEFT JOIN AdminBooks b ON l.bookId = b.bookId " + // ADMIN_BOOKS -> AdminBooks
            "LEFT JOIN b.bookIsbn bi " +               // 관계 매핑 이용 (AdminBooks 엔티티의 bookIsbn 필드)
            "WHERE (:type IS NULL OR :type = '전체' OR an.notiType = :type) " +
            "AND (:keyword IS NULL OR u.userNm LIKE %:keyword% OR u.userId LIKE %:keyword% OR CAST(l.lendId AS string) LIKE %:keyword% OR bi.bookTitle LIKE %:keyword%)")
    Page<AdminNotiResponseDto> findDetailedNotifications(
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    // [수정 완료] UPDATE문도 자바 엔티티 기준
    @Query("UPDATE AdminNotification n SET n.checkYn = 'Y', n.updatedAt = :now WHERE n.checkYn = 'N'")
    int markAllAsRead(@Param("now") LocalDateTime now);
}