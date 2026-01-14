package com.booknara.booknaraPrj.admin.statistics;

import com.booknara.booknaraPrj.admin.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatisticsRepository extends JpaRepository<Users,String> {
    // 1. 실시간 연령대 분포 쿼리
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 20 THEN '10대 이하' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 30 THEN '20대' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 40 THEN '30대' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 50 THEN '40대' " +
            "  ELSE '50대 이상' " +
            "END AS label, " +
            "COUNT(*) AS count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM USERS), 1) AS percentage " +
            "FROM USERS " +
            "GROUP BY label " +
            "ORDER BY label", nativeQuery = true)
    List<UserAgeStatProjection> findAgeGroupStatistics();

    // 2. 실시간 성별 분포 쿼리
    @Query(value = "SELECT " +
            "GENDER AS label, " +
            "COUNT(*) AS count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM USERS), 1) AS percentage " +
            "FROM USERS " +
            "GROUP BY GENDER", nativeQuery = true)
    List<UserAgeStatProjection> findGenderStatistics();

    // 성별별 연령대 통계 (gender 파라미터: 'M' 또는 'F')
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 20 THEN '10대 이하' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 30 THEN '20대' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 40 THEN '30대' " +
            "  WHEN (YEAR(CURDATE()) - YEAR(BIRTHDAY)) < 50 THEN '40대' " +
            "  ELSE '50대 이상' " +
            "END AS label, " +
            "COUNT(*) AS count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM USERS WHERE GENDER = :gender), 1) AS percentage " +
            "FROM USERS " +
            "WHERE GENDER = :gender " +
            "GROUP BY LABEL " +
            "ORDER BY LABEL", nativeQuery = true)
    List<UserAgeStatProjection> findAgeGroupStatisticsByGender(@Param("gender") String gender);
}
