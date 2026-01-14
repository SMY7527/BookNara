package com.booknara.booknaraPrj.reviewstatus.mapper;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * [ReviewStatusMapper]
 * 도서별 리뷰 통계(평균 별점, 리뷰 개수)를 DB에서 집계하여 조회하는 매퍼입니다.
 */
@Mapper
public interface ReviewStatusMapper {

    /**
     * [단건 조회] 도서 상세 페이지용
     * - 특정 도서의 평균 별점과 총 리뷰 수를 실시간 집계합니다.
     */
    ReviewStatusDTO selectByIsbn(@Param("isbn13") String isbn13);

    /**
     * [다건 일괄 조회] 검색 목록 및 그리드 뷰용
     * - 여러 ISBN 리스트를 한 번에 넘겨 N+1 문제를 원천 차단합니다.
     * - MyBatis의 <foreach> 태그를 통해 SQL의 'IN' 절로 변환됩니다.
     */
    List<ReviewStatusDTO> selectByIsbns(@Param("isbns") List<String> isbns);
}