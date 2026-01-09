package com.booknara.booknaraPrj.feed.review.service;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewListDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import com.booknara.booknaraPrj.feed.review.mapper.FeedReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ReviewListDTO getPage(String isbn13, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50); // 폭탄 방지
        int offset = (safePage - 1) * safeSize;

        long total = mapper.countByIsbn(isbn13);
        List<ReviewItemDTO> items = (total == 0)
                ? Collections.emptyList()
                : mapper.selectPageByIsbn(isbn13, offset, safeSize);

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
}
