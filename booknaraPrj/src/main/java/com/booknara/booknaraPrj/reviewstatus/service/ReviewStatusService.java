package com.booknara.booknaraPrj.reviewstatus.service;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import com.booknara.booknaraPrj.reviewstatus.mapper.ReviewStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * [ReviewStatusService]
 * 도서 상세 및 목록 페이지에 표시될 리뷰 통계(별점 평균, 리뷰 개수)를 관리하는 서비스입니다.
 * 데이터 조회 시 발생할 수 있는 입력 예외를 처리하고 매퍼와의 가교 역할을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class ReviewStatusService {

    private final ReviewStatusMapper reviewStatusMapper;

    /**
     * [단건 리뷰 상태 조회]
     * - 도서 상세 페이지에서 해당 도서의 별점 정보를 가져올 때 사용합니다.
     * @param isbn13 조회할 도서의 ISBN
     * @return 통계 정보 DTO (입력값이 유효하지 않을 경우 null 반환)
     */
    public ReviewStatusDTO getByIsbn(String isbn13) {
        // [방어 코드] ISBN이 없거나 빈 문자열인 경우 DB를 조회하지 않고 즉시 리턴
        if (isbn13 == null || isbn13.isBlank()) return null;

        return reviewStatusMapper.selectByIsbn(isbn13);
    }

    /**
     * [다건 리뷰 상태 일괄 조회]
     * - 검색 결과 리스트나 도서 그리드 화면에서 여러 도서의 별점을 한 번에 표시할 때 사용합니다.
     * @param isbns 조회할 ISBN 리스트
     * @return 통계 정보 리스트 (입력값이 없으면 빈 리스트 반환)
     */
    public List<ReviewStatusDTO> getByIsbns(List<String> isbns) {
        // [방어 코드] 리스트가 비어있는 경우 null 대신 빈 리스트를 반환하여 NPE 방지
        if (isbns == null || isbns.isEmpty()) return Collections.emptyList();

        // 매퍼를 통해 단 한 번의 쿼리로 다건 데이터를 효율적으로 조회 (Batch Fetching)
        return reviewStatusMapper.selectByIsbns(isbns);
    }
}