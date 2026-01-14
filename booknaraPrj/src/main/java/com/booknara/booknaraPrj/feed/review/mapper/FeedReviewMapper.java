package com.booknara.booknaraPrj.feed.review.mapper;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * [FeedReviewMapper]
 * 도서 리뷰와 관련된 모든 DB 접근을 담당합니다.
 * FEEDS(공통 피드)와 REVIEW_DETAIL(별점/도서 정보) 테이블을 유기적으로 연결합니다.
 */
@Mapper
public interface FeedReviewMapper {

    /** [통계] 특정 도서의 평균 별점 및 총 리뷰 수 조회 */
    ReviewSummaryDTO selectSummaryByIsbn(@Param("isbn13") String isbn13);

    /** [페이징] 특정 도서의 전체 리뷰 개수 카운트 */
    long countByIsbn(@Param("isbn13") String isbn13);

    /** [조회] 특정 도서의 리뷰 목록 (비로그인용/기본) */
    List<ReviewItemDTO> selectPageByIsbn(@Param("isbn13") String isbn13,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    /** [권한] 해당 사용자가 도서를 '반납'했는지 확인 (리뷰 작성 자격 검증) */
    int existsReturnedLend(@Param("userId") String userId,
                           @Param("isbn13") String isbn13);

    /** [조회] 특정 도서에 대해 사용자가 이미 작성한 리뷰가 있는지 확인 및 ID 반환 */
    String selectMyReviewFeedId(@Param("userId") String userId,
                                @Param("isbn13") String isbn13);

    /** [저장-1] 공통 피드 테이블(FEEDS)에 제목/내용 저장 */
    int insertReviewFeed(@Param("feedId") String feedId,
                         @Param("userId") String userId,
                         @Param("isbn13") String isbn13,
                         @Param("title") String title,
                         @Param("content") String content);

    /** [저장-2] 리뷰 상세 테이블(REVIEW_DETAIL)에 별점 저장 */
    int insertReviewDetail(@Param("feedId") String feedId,
                           @Param("rate") int rate);

    /** [수정-1] 피드 제목/내용 수정 (작성자 본인 확인 포함) */
    int updateReviewFeedByOwner(@Param("feedId") String feedId,
                                @Param("userId") String userId,
                                @Param("title") String title,
                                @Param("content") String content);

    /** [수정-2] 리뷰 별점 수정 */
    int updateReviewDetail(@Param("feedId") String feedId,
                           @Param("rate") int rate);

    /** [조회-로그인용] 본인 글 여부(mineYn)를 포함한 리뷰 목록 조회 */
    List<ReviewItemDTO> selectPageByIsbnWithMine(
            @Param("isbn13") String isbn13,
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /** [유틸] 피드 ID로 대상 도서의 ISBN 조회 */
    String selectIsbnByFeedId(@Param("feedId") String isbn13);

    /** [삭제] 리뷰 피드 삭제 (작성자 본인 확인 필수) */
    int deleteReviewFeedByOwner(@Param("feedId") String feedId,
                                @Param("userId") String userId);
}