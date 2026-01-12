package com.booknara.booknaraPrj.bookDetail.service;

import com.booknara.booknaraPrj.bookDetail.dto.*;
import com.booknara.booknaraPrj.bookDetail.mapper.BookDetailMapper;
import com.booknara.booknaraPrj.bookMark.service.BookmarkService;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import com.booknara.booknaraPrj.feed.review.service.FeedReviewService;
import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import com.booknara.booknaraPrj.reviewstatus.service.ReviewStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookDetailService {

    private final BookDetailMapper bookDetailMapper;
    private final FeedReviewService feedReviewService;
    private final ReviewStatusService reviewStatusService;

    @Transactional(readOnly = true)
    public BookDetailViewDTO getBookDetailView(String isbn13, String userId) {

        // 1) 도서 메타
        BookDetailDTO detail = bookDetailMapper.selectBookDetail(isbn13);
        if (detail == null) return null;

        // 2) 재고 집계
        BookInventoryDTO inventory = bookDetailMapper.selectInventory(isbn13);
        if (inventory == null) {
            inventory = new BookInventoryDTO();
            inventory.setTotalCount(0);
            inventory.setAvailableCount(0);
            inventory.setLostCount(0);
        }

        // 3) breadcrumb
        GenrePathDTO genrePath = buildGenrePath(detail.getGenreId());

        // 4) ViewDTO 조립
        BookDetailViewDTO view = new BookDetailViewDTO();
        view.setBookDetailDTO(detail);
        view.setInventory(inventory);
        view.setGenrePath(genrePath);

        // 5) 전자책/종이책  링크
        // 5) 전자책/종이책 링크 계산
        String pairIsbn = calcPairIsbn(detail.getIsbn13(), detail.getEbookYn());

        if (pairIsbn != null && !pairIsbn.equals(detail.getIsbn13())) {
            int cnt = bookDetailMapper.existsByIsbn(pairIsbn);
            if (cnt > 0) {
                view.setPairYn("Y");
                view.setPairIsbn13(pairIsbn);
            } else {
                view.setPairYn("N");
            }
        } else {
            view.setPairYn("N");
        }

        // 6) 리뷰
        ReviewStatusDTO rs = reviewStatusService.getByIsbn(isbn13);
        ReviewSummaryDTO summary = new ReviewSummaryDTO();
        summary.setReviewCnt(rs == null ? 0 : rs.getReviewCnt());
        summary.setRatingAvg(rs == null ? 0.0 : rs.getRatingAvg());
        view.setReviewSummary(summary);
        view.setReviewPreview(feedReviewService.getTop(isbn13, 5));

        return view;
    }


    //전자책 링크 조회 매서드
    private String calcPairIsbn(String isbn13, String ebookYn){
        if(isbn13 == null) return null;

        boolean isEbook = "Y".equalsIgnoreCase(ebookYn);

        if(isEbook){
            // ebook -> paper
            if(isbn13.endsWith("_e")) return isbn13.substring(0, isbn13.length()-2);
            return isbn13;
        }else{
            // paper -> ebook
            if(isbn13.endsWith("_e")) return isbn13;
            return isbn13 + "_e";
        }
    }


    private GenrePathDTO buildGenrePath(Integer genreId) {
        GenrePathDTO path = new GenrePathDTO();
        if (genreId == null) return path;

        Map<String, Object> row = bookDetailMapper.selectGenreSelfAndParent(genreId);
        if (row == null || row.isEmpty()) return path;

        String mall = (String) row.get("mall");
        Integer selfId = toInteger(row.get("genreId"));
        String selfNm = (String) row.get("genreNm");
        Integer parentId = toInteger(row.get("parentId"));
        String parentNm = (String) row.get("parentNm");

        path.setMall(mall);

        if (parentId != null && parentNm != null) {
            path.getCrumbs().add(new GenreCrumbDTO(parentId, parentNm));
        }
        if (selfId != null && selfNm != null) {
            path.getCrumbs().add(new GenreCrumbDTO(selfId, selfNm));
        }
        return path;
    }

    private Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Short) return ((Short) o).intValue();
        if (o instanceof String) return Integer.valueOf((String) o);
        throw new IllegalArgumentException("Cannot convert to Integer: " + o.getClass());
    }
}
