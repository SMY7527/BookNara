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

    int existsReturnedLend(@Param("userId") String userId,
                           @Param("isbn13") String isbn13);

    String selectMyReviewFeedId(@Param("userId") String userId,
                                @Param("isbn13") String isbn13);

    int insertReviewFeed(@Param("feedId") String feedId,
                         @Param("userId") String userId,
                         @Param("isbn13") String isbn13,
                         @Param("title") String title,
                         @Param("content") String content);

    int insertReviewDetail(@Param("feedId") String feedId,
                           @Param("rate") int rate);

    int updateReviewFeedByOwner(@Param("feedId") String feedId,
                                @Param("userId") String userId,
                                @Param("title") String title,
                                @Param("content") String content);

    int updateReviewDetail(@Param("feedId") String feedId,
                           @Param("rate") int rate);

    List<ReviewItemDTO> selectPageByIsbnWithMine(
            @Param("isbn13") String isbn13,
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    String selectIsbnByFeedId(@Param("feedId") String feedId);

    int deleteReviewFeedByOwner(@Param("feedId") String feedId,
                                @Param("userId") String userId);

}
