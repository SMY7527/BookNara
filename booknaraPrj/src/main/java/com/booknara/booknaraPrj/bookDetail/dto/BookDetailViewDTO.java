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
    private String pairIsbn13;   // 연결될 ISBN
    private String pairLabel;    // "전자책 보기" or "종이책 보기"
    private String pairYn;       // "Y/N" (선택)


    private ReviewSummaryDTO reviewSummary;      // 평균/개수
    private List<ReviewItemDTO> reviewPreview;   // 미리보기

    private String bookmarkedYn;              // view.bookmarkedYn (Y/N)
    private int bookmarkCnt;                // view.bookmarkCnt
}
