package com.booknara.booknaraPrj.reviewstatus.dto;

import lombok.Data;
import java.util.List;

/**
 * [ReviewStatusBatchRequestDTO]
 * 여러 도서의 리뷰 통계 정보를 한 번에 요청하기 위한 데이터 전송 객체입니다.
 * 주로 목록 페이지(검색, 베스트셀러 등)에서 성능 최적화를 위해 사용됩니다.
 */
@Data
public class ReviewStatusBatchRequestDTO {

    /** * 상태 조회가 필요한 도서들의 ISBN13 목록
     * 클라이언트(프론트엔드)는 화면에 노출된 도서들의 ISBN을 리스트에 담아 전달합니다.
     */
    private List<String> isbn13List;
}