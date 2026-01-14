package com.booknara.booknaraPrj.admin.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LendRepository extends JpaRepository<Lend, String> {

    // [1] JPQL: 엔티티 클래스 이름(Lend)과 필드명(lendDate)을 사용해야 합니다.
    @Query("SELECT new map(FUNCTION('DATE_FORMAT', l.lendDate, '%Y-%m') as label, COUNT(l) as count) " +
            "FROM Lend l " +
            "WHERE l.lendDate >= :startDate AND l.lendDate <= :endDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', l.lendDate, '%Y-%m') " +
            "ORDER BY label ASC")
    List<Map<String, Object>> findMonthlyLendStats(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // [2] Query Methods: 필드명 기준으로 자동 생성됩니다.
    long countByReturnDoneAtIsNull();

    long countByOverDueAndReturnDoneAtIsNull(String overDue);

    long count();

    // [3] Native Query: 실제 DB 테이블명인 'LENDS'와 컬럼명을 사용해야 합니다.
    @Query(value = "SELECT COUNT(*) FROM LENDS WHERE " +
            "(RETURN_DONE_AT IS NULL AND RETURN_DUE_DATE < :now) OR " +
            "(RETURN_DONE_AT > RETURN_DUE_DATE)", nativeQuery = true)
    long countAllOverdueItems(@Param("now") LocalDateTime now);

    long countByExtendCntGreaterThan(int count);

    // [4] Native Query: 테이블명 'LENDS'로 수정
    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, LEND_DATE)), 0) " +
            "FROM LENDS WHERE RETURN_DONE_AT IS NOT NULL", nativeQuery = true)
    Double getAvgLendDays();

    // [5] Native Query: 테이블명 'LENDS'로 수정
    @Query(value = "SELECT COALESCE(AVG(DATEDIFF(RETURN_DONE_AT, RETURN_DUE_DATE)), 0) " +
            "FROM LENDS WHERE RETURN_DONE_AT > RETURN_DUE_DATE", nativeQuery = true)
    Double getAvgOverdueDays();
}