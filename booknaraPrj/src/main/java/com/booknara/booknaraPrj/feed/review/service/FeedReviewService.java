package com.booknara.booknaraPrj.feed.review.service;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewListDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import com.booknara.booknaraPrj.feed.review.mapper.FeedReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.booknara.booknaraPrj.feed.review.dto.ReviewPermissionDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSaveRequestDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedReviewService {

    private final FeedReviewMapper mapper;

    /** ISBN별 리뷰 요약(리뷰 수/평점 평균) */
    public ReviewSummaryDTO getSummary(String isbn13) {
        ReviewSummaryDTO summary = mapper.selectSummaryByIsbn(isbn13);

        // 리뷰가 하나도 없으면 summary가 null일 수 있으니 기본값 보정
        if (summary == null) {
            summary = new ReviewSummaryDTO();
            summary.setIsbn13(isbn13);
            summary.setReviewCnt(0);
            summary.setRatingAvg(0.0);
            return summary;
        }

        if (summary.getReviewCnt() == null) summary.setReviewCnt(0);
        if (summary.getRatingAvg() == null) summary.setRatingAvg(0.0);

        return summary;
    }

    /** ISBN별 리뷰 총 개수 */
    public long count(String isbn13) {
        return mapper.countByIsbn(isbn13);
    }

    /** ISBN별 리뷰 페이지 조회 */
    public ReviewListDTO getPage(String isbn13, int page, int size, String userId) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;

        long total = mapper.countByIsbn(isbn13);

        List<ReviewItemDTO> items = (total == 0)
                ? Collections.emptyList()
                : mapper.selectPageByIsbnWithMine(isbn13, userId, offset, safeSize); // ✅ userId 포함 쿼리

        ReviewListDTO dto = new ReviewListDTO();
        dto.setIsbn13(isbn13);
        dto.setSummary(getSummary(isbn13));
        dto.setItems(items);
        dto.setPage(safePage);
        dto.setSize(safeSize);
        dto.setTotal(total);

        return dto;
    }


    /** 상세페이지 미리보기용 Top N (최신순) */
    public List<ReviewItemDTO> getTop(String isbn13, int topN) {
        int safeSize = Math.min(Math.max(topN, 1), 20);
        return mapper.selectPageByIsbn(isbn13, 0, safeSize);
    }


    @Transactional(readOnly = true)
    public ReviewPermissionDTO getReviewPermission(String isbn13, String userId) {
        ReviewPermissionDTO dto = new ReviewPermissionDTO();
        dto.setIsbn13(isbn13);

        if (userId == null || userId.isBlank()) {
            dto.setAllowedYn("N");
            dto.setHasReviewYn("N");
            dto.setMyFeedId(null);
            dto.setMessage("로그인 후 이용 가능합니다.");
            return dto;
        }

        int ok = mapper.existsReturnedLend(userId, isbn13);
        dto.setAllowedYn(ok == 1 ? "Y" : "N");

        String feedId = mapper.selectMyReviewFeedId(userId, isbn13);
        dto.setHasReviewYn(feedId != null ? "Y" : "N");
        dto.setMyFeedId(feedId);

        if ("N".equals(dto.getAllowedYn())) dto.setMessage("반납 완료 이력이 있는 도서만 리뷰 작성이 가능합니다.");
        else if ("Y".equals(dto.getHasReviewYn())) dto.setMessage("이미 작성한 리뷰가 있어 수정할 수 있습니다.");
        else dto.setMessage("리뷰 작성이 가능합니다.");

        return dto;
    }

    @Transactional
    public String saveReview(ReviewSaveRequestDTO req, String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalStateException("로그인이 필요합니다.");
        if (req == null) throw new IllegalArgumentException("요청값이 없습니다.");
        if (req.getIsbn13() == null || req.getIsbn13().isBlank()) throw new IllegalArgumentException("isbn13 필요");
        if (req.getRate() == null || req.getRate() < 1 || req.getRate() > 5) throw new IllegalArgumentException("평점은 1~5");
        if (req.getTitle() != null && req.getTitle().length() > 300) throw new IllegalArgumentException("제목이 너무 깁니다.");
        // content는 MEDIUMTEXT라 길이 제한은 UI에서 적당히 컷 추천

        // 반납 이력 체크(권한)
        if (mapper.existsReturnedLend(userId, req.getIsbn13()) != 1) {
            throw new IllegalStateException("반납 완료 이력이 있는 도서만 리뷰 작성이 가능합니다.");
        }

        String myFeedId = mapper.selectMyReviewFeedId(userId, req.getIsbn13());

        if (myFeedId == null) {
            String feedId = "F_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "_" + UUID.randomUUID().toString().substring(0, 6);

            mapper.insertReviewFeed(feedId, userId, req.getIsbn13(), req.getTitle(), req.getContent());
            mapper.insertReviewDetail(feedId, req.getRate());
            return feedId;
        } else {
            int u1 = mapper.updateReviewFeedByOwner(myFeedId, userId, req.getTitle(), req.getContent());
            if (u1 != 1) throw new IllegalStateException("본인 리뷰만 수정할 수 있습니다.");
            mapper.updateReviewDetail(myFeedId, req.getRate());
            return myFeedId;
        }
    }

    @Transactional
    public void deleteMyReview(String feedId, String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalStateException("로그인이 필요합니다.");
        if (feedId == null || feedId.isBlank()) throw new IllegalArgumentException("feedId 필요");

        int updated = mapper.deleteReviewFeedByOwner(feedId, userId);
        if (updated != 1) {
            // 이미 삭제됐거나 / 남의 글 / 존재하지 않음
            throw new IllegalStateException("본인 리뷰만 삭제할 수 있습니다.");
        }

    }

}
