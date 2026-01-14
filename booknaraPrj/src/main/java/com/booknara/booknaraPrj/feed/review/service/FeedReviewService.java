package com.booknara.booknaraPrj.feed.review.service;

import com.booknara.booknaraPrj.feed.review.dto.*;
import com.booknara.booknaraPrj.feed.review.mapper.FeedReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

/**
 * [FeedReviewService]
 * 도서 리뷰의 라이프사이클(조회, 권한 검증, 저장, 삭제)을 관리하는 핵심 서비스입니다.
 * '반납 완료 도서'에 한해 리뷰 작성을 허용하는 신뢰 기반 리뷰 정책을 구현합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정하여 성능 최적화
public class FeedReviewService {

    private final FeedReviewMapper mapper;

    /**
     * [ISBN별 리뷰 통계 조회]
     * 특정 도서의 총 리뷰 수와 평균 평점을 계산합니다.
     * @return 리뷰가 없는 경우 기본값(0, 0.0)이 채워진 DTO 반환
     */
    public ReviewSummaryDTO getSummary(String isbn13) {
        ReviewSummaryDTO summary = mapper.selectSummaryByIsbn(isbn13);

        // 데이터가 없는 도서의 경우 Null-Safety 처리
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

    /** 특정 도서의 전체 리뷰 개수 조회 */
    public long count(String isbn13) {
        return mapper.countByIsbn(isbn13);
    }

    /**
     * [리뷰 목록 페이징 조회]
     * 사용자의 로그인 상태에 따라 '본인 작성 여부(mineYn)'를 포함한 목록을 반환합니다.
     */
    public ReviewListDTO getPage(String isbn13, int page, int size, String userId) {
        // 안전한 페이징 파라미터 보정
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;

        long total = mapper.countByIsbn(isbn13);

        // 데이터가 없을 경우 빈 리스트 반환, 있을 경우 mineYn 포함 쿼리 실행
        List<ReviewItemDTO> items = (total == 0)
                ? Collections.emptyList()
                : mapper.selectPageByIsbnWithMine(isbn13, userId, offset, safeSize);

        ReviewListDTO dto = new ReviewListDTO();
        dto.setIsbn13(isbn13);
        dto.setSummary(getSummary(isbn13)); // 요약 통계 함께 포함
        dto.setItems(items);
        dto.setPage(safePage);
        dto.setSize(safeSize);
        dto.setTotal(total);

        return dto;
    }

    /** 도서 상세 페이지 미리보기용 최신 리뷰 목록 조회 (최대 20건 제한) */
    public List<ReviewItemDTO> getTop(String isbn13, int topN) {
        int safeSize = Math.min(Math.max(topN, 1), 20);
        return mapper.selectPageByIsbn(isbn13, 0, safeSize);
    }

    /**
     * [리뷰 작성 및 수정 권한 검증]
     * 로그인 여부, 반납 완료 이력, 기존 작성 여부를 종합 판단하여 UI 제어에 필요한 정보를 제공합니다.
     */
    public ReviewPermissionDTO getReviewPermission(String isbn13, String userId) {
        ReviewPermissionDTO dto = new ReviewPermissionDTO();
        dto.setIsbn13(isbn13);

        // 1. 로그인 체크
        if (userId == null || userId.isBlank()) {
            dto.setAllowedYn("N");
            dto.setHasReviewYn("N");
            dto.setMessage("로그인 후 이용 가능합니다.");
            return dto;
        }

        // 2. 반납 이력 확인 (Verified Review 정책)
        int ok = mapper.existsReturnedLend(userId, isbn13);
        dto.setAllowedYn(ok == 1 ? "Y" : "N");

        // 3. 기존 작성 이력 확인 (1인 1리뷰 정책)
        String feedId = mapper.selectMyReviewFeedId(userId, isbn13);
        dto.setHasReviewYn(feedId != null ? "Y" : "N");
        dto.setMyFeedId(feedId);

        // 4. 상황별 사유 메시지 세팅
        if ("N".equals(dto.getAllowedYn())) {
            dto.setMessage("반납 완료 이력이 있는 도서만 리뷰 작성이 가능합니다.");
        } else if ("Y".equals(dto.getHasReviewYn())) {
            dto.setMessage("이미 작성한 리뷰가 있어 수정할 수 있습니다.");
        } else {
            dto.setMessage("리뷰 작성이 가능합니다.");
        }

        return dto;
    }

    /**
     * [리뷰 저장 및 수정 실행]
     * 반납 이력을 최종 검증한 후, 신규 등록(UUID 생성) 또는 기존 글 수정을 수행합니다.
     * FEEDS와 REVIEW_DETAIL 두 테이블에 대한 원자적 트랜잭션을 보장합니다.
     */
    @Transactional
    public String saveReview(ReviewSaveRequestDTO req, String userId) {
        // [방어 코드] 입력값 및 로그인 유효성 검사
        if (userId == null || userId.isBlank()) throw new IllegalStateException("로그인이 필요합니다.");
        if (req == null || req.getIsbn13() == null) throw new IllegalArgumentException("필수 요청값이 없습니다.");
        if (req.getRate() == null || req.getRate() < 1 || req.getRate() > 5) throw new IllegalArgumentException("평점은 1~5점 사이여야 합니다.");

        // [권한 재검증] 실제 반납 이력이 있는지 최종 확인 (API 우회 공격 방지)
        if (mapper.existsReturnedLend(userId, req.getIsbn13()) != 1) {
            throw new IllegalStateException("리뷰 작성 권한이 없습니다.");
        }

        String myFeedId = mapper.selectMyReviewFeedId(userId, req.getIsbn13());

        if (myFeedId == null) {
            // [신규 등록 케이스] 고유 피드 ID 생성 (날짜 + 랜덤 UUID 조합)
            String feedId = "F_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "_" + UUID.randomUUID().toString().substring(0, 6);

            mapper.insertReviewFeed(feedId, userId, req.getIsbn13(), req.getTitle(), req.getContent());
            mapper.insertReviewDetail(feedId, req.getRate());
            return feedId;
        } else {
            // [수정 케이스] 소유자 확인을 포함한 업데이트 수행
            int u1 = mapper.updateReviewFeedByOwner(myFeedId, userId, req.getTitle(), req.getContent());
            if (u1 != 1) throw new IllegalStateException("본인 리뷰만 수정할 수 있습니다.");
            mapper.updateReviewDetail(myFeedId, req.getRate());
            return myFeedId;
        }
    }

    /** [리뷰 삭제] 작성자 본인 확인 절차를 포함한 논리 삭제(IS_DELETED='Y')를 수행합니다. */
    @Transactional
    public void deleteMyReview(String feedId, String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalStateException("로그인이 필요합니다.");

        int updated = mapper.deleteReviewFeedByOwner(feedId, userId);
        if (updated != 1) {
            throw new IllegalStateException("삭제 권한이 없거나 이미 삭제된 리뷰입니다.");
        }
    }
}