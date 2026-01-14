/*
package com.booknara.booknaraPrj.admin.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LendRepository extends JpaRepository<Lend, String> {

    // 연도-월별 통계를 위한 전용 쿼리 (MariaDB/MySQL 기준)
    @Query("SELECT new map(FUNCTION('DATE_FORMAT', L.LEND_DATE, '%Y-%m') as LABEL, COUNT(L) as COUNT) " +
            "FROM LEND L " +
            "WHERE L.LEND_DATE >= :startDate AND L.LEND_DATE <= :endDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', L.LEND_DATE, '%Y-%m') " +
            "ORDER BY LABEL ASC")
    List<Map<String, Object>> findMonthlyLendStats(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // 현재 대여 중인 도서 건수
    long countByReturnDoneAtIsNull();

    // 연체 중인 도서 건수
    long countByOverDueAndReturnDoneAtIsNull(String overDue);

    // [1] 전체 대여 건수
    long count();

    // [2] 통합 연체 건수 (Native Query 시 파라미터 타입을 String으로 변환해서 던지는 것이 안전할 때가 있습니다)
    @Query(value = "SELECT COUNT(*) FROM LENDS WHERE " +
            "(RETURN_DONE_AT IS NULL AND RETURN_DUE_DATE < :now) OR " +
            "(RETURN_DONE_AT > RETURN_DUE_DATE)", nativeQuery = true)
    long countAllOverdueItems(@Param("now") LocalDateTime now);

    // [3] 연장 횟수 기반
    long countByExtendCntGreaterThan(int count);

    // [4] 평균 대여 일수 (COALESCE를 사용하여 null일 경우 0을 반환하도록 강제)
    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, LEND_DATE)), 0) " +
            "FROM LENDS WHERE RETURN_DONE_AT IS NOT NULL", nativeQuery = true)
    Double getAvgLendDays();

    // [5] 평균 연체 일수 (DATEDIFF 순서: 늦은 날짜가 앞이어야 양수가 나옵니다)
    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, RETURN_DUE_DATE)), 0) " +
            "FROM LENDS WHERE RETURN_DONE_AT > RETURN_DUE_DATE", nativeQuery = true)
    Double getAvgOverdueDays();
}*/

package com.booknara.booknaraPrj.admin.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LendRepository extends JpaRepository<Lend, String> { // ID 타입이 String인지 Long인지 확인 필요

    // [수정 완료]
    // 1. ORDER BY LABEL -> ORDER BY FUNCTION(...) (식 전체를 복사)
    // 2. FROM Lend, l.lendDate (자바 엔티티/변수명 유지)
    @Query("SELECT new map(FUNCTION('DATE_FORMAT', l.lendDate, '%Y-%m') as LABEL, COUNT(l) as COUNT) " +
            "FROM Lend l " +
            "WHERE l.lendDate >= :startDate AND l.lendDate <= :endDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', l.lendDate, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', l.lendDate, '%Y-%m') ASC") // 👈 여기를 수정했습니다!
    List<Map<String, Object>> findMonthlyLendStats(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // ... (나머지 코드는 기존 수정본 유지) ...
    long countByReturnDoneAtIsNull();

    long countByOverDueAndReturnDoneAtIsNull(String overDue);

    long count();

    @Query(value = "SELECT COUNT(*) FROM LEND WHERE " +
            "(RETURN_DONE_AT IS NULL AND RETURN_DUE_DATE < :now) OR " +
            "(RETURN_DONE_AT > RETURN_DUE_DATE)", nativeQuery = true)
    long countAllOverdueItems(@Param("now") LocalDateTime now);

    long countByExtendCntGreaterThan(int count);

    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, LEND_DATE)), 0) " +
            "FROM LEND WHERE RETURN_DONE_AT IS NOT NULL", nativeQuery = true)
    Double getAvgLendDays();

    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, RETURN_DUE_DATE)), 0) " +
            "FROM LEND WHERE RETURN_DONE_AT > RETURN_DUE_DATE", nativeQuery = true)
    Double getAvgOverdueDays();
}