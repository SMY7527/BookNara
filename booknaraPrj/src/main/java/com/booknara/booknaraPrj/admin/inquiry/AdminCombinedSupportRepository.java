package com.booknara.booknaraPrj.admin.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminCombinedSupportRepository extends JpaRepository<AdminCombinedSupport, String> {

    @Query(value = "SELECT * FROM VIEW_COMBINED_SUPPORT V " +
            "WHERE (:type = 'ALL' OR V.TYPE = :type) " +
            "AND (:status = 'ALL' OR V.STATE = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR V.USER_ID LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY " +
            "CASE WHEN :sortField = 'regDate' AND :sortDir = 'asc' THEN V.REG_DATE END ASC, " +
            "CASE WHEN :sortField = 'regDate' AND :sortDir = 'desc' THEN V.REG_DATE END DESC, " +
            "CASE WHEN :sortField = 'resolvedAt' AND :sortDir = 'asc' THEN V.RESOLVED_AT END ASC, " +
            "CASE WHEN :sortField = 'resolvedAt' AND :sortDir = 'desc' THEN V.RESOLVED_AT END DESC, " +
            "V.REG_DATE DESC",
            countQuery = "SELECT COUNT(*) FROM VIEW_COMBINED_SUPPORT V " +
                    "WHERE (:type = 'ALL' OR V.TYPE = :type) " +
                    "AND (:status = 'ALL' OR V.STATE = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR V.USER_ID LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<AdminCombinedSupport> findFilteredList(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("status") String status,
            @Param("sortField") String sortField,
            @Param("sortDir") String sortDir,
            Pageable pageable);

    long countByState(String state);
}