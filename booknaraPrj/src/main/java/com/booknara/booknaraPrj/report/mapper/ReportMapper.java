package com.booknara.booknaraPrj.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper {

    int existsActiveReviewFeed(@Param("feedId") String feedId);

    int existsReport(@Param("userId") String userId,
                     @Param("feedId") String feedId);

    int insertReport(@Param("reportId") String reportId,
                     @Param("userId") String userId,
                     @Param("feedId") String feedId,
                     @Param("reportContent") String reportContent);
}
