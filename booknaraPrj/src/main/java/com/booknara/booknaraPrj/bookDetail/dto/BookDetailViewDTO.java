package com.booknara.booknaraPrj.bookDetail.dto;

import com.booknara.booknaraPrj.feed.review.dto.ReviewItemDTO;
import com.booknara.booknaraPrj.feed.review.dto.ReviewSummaryDTO;
import lombok.Data;

import java.util.List;

@Data
public class BookDetailViewDTO {
    private BookDetailDTO bookDetailDTO;
    private GenrePathDTO genrePath;
    private BookInventoryDTO inventory;

    private ReviewSummaryDTO reviewSummary;      // 평균/개수
    private List<ReviewItemDTO> reviewPreview;   // 미리보기

    //  북마크
    private String bookmarkedYn; // 'Y'/'N' (로그인 안했으면 'N')
    private int bookmarkCnt;     // 해당 ISBN 북마크 총 수
}
