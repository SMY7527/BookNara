package com.booknara.booknaraPrj.feed.review.mapper;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedReviewMapper {

    ReviewSummaryDTO selectSummaryByIsbn(@Param("isbn13") String isbn13);

    long countByIsbn(@Param("isbn13") String isbn13);

    List<ReviewItemDTO> selectPageByIsbn(@Param("isbn13") String isbn13,
                                         @Param("offset") int offset,
                                         @Param("size") int size);
}
