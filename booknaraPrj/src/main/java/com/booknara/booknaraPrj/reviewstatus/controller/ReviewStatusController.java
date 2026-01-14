package com.booknara.booknaraPrj.reviewstatus.controller;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import com.booknara.booknaraPrj.reviewstatus.service.ReviewStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [ReviewStatusController]
 * 도서의 리뷰 통계(평균 별점, 리뷰 개수)를 제공하는 REST 컨트롤러입니다.
 * 단건 상세 조회와 다건 일괄 조회를 모두 지원하여 UI 성능 최적화에 기여합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/reviewstatus")
public class ReviewStatusController {

    private final ReviewStatusService reviewStatusService;

    /**
     * [도서 상세용: 단건 리뷰 상태 조회]
     * - URL: /book/reviewstatus/{isbn13}
     * - 역할: 특정 도서의 별점과 리뷰 수를 반환합니다.
     */
    @GetMapping("/{isbn13}")
    public ResponseEntity<ReviewStatusDTO> getOne(@PathVariable String isbn13) {
        ReviewStatusDTO dto = reviewStatusService.getByIsbn(isbn13);
        // 리뷰가 없는 신규 도서의 경우 null이 반환될 수 있으나,
        // 클라이언트에서 0이나 빈 별점으로 처리하기 가장 유연한 상태로 응답합니다.
        return ResponseEntity.ok(dto);
    }

    /**
     * [목록/검색용: 다건 리뷰 상태 일괄 조회]
     * - URL: /book/reviewstatus?isbns=isbn1,isbn2,isbn3
     * - 역할: 쉼표(,)로 구분된 ISBN 목록을 받아 각 도서의 리뷰 정보를 한 번에 반환합니다.
     * - 특징: N+1 문제를 방지하기 위해 단일 요청으로 여러 데이터를 가져오는 Batch API입니다.
     */
    @GetMapping
    public ResponseEntity<List<ReviewStatusDTO>> getMany(@RequestParam(name = "isbns") String isbns) {
        // 1. 쉼표 기준 분리 -> 공백 제거 -> 빈 문자열 제외 -> 중복 제거 순으로 정제
        List<String> list = Arrays.stream(isbns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 2. 파라미터가 유효하지 않으면 빈 리스트 즉시 반환
        if (list.isEmpty()) return ResponseEntity.ok(List.of());

        // 3. 서비스 레이어 호출하여 일괄 조회 결과 반환
        return ResponseEntity.ok(reviewStatusService.getByIsbns(list));
    }
}