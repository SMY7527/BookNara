package com.booknara.booknaraPrj.common.dto;

import lombok.Getter;
import java.util.List;

/**
 * 공통 페이지네이션 응답 DTO
 *
 * <p>
 * - 검색, 목록 조회 API에서 공통으로 사용하는 페이지 결과 래퍼
 * - 실제 데이터 목록(items) + 페이지네이션 메타 정보(page, size, total 등)를 함께 전달
 * </p>
 *
 * @param <T> 페이지에 포함될 데이터 타입
 *           (예: BookSearchDTO, BookmarkDTO 등)
 */
@Getter
public class PageResultDTO<T> {

    /**
     * 현재 페이지에 포함된 데이터 목록
     */
    private final List<T> items;

    /**
     * 현재 페이지 번호 (1부터 시작)
     */
    private final int page;

    /**
     * 페이지당 데이터 개수
     */
    private final int size;

    /**
     * 전체 데이터 개수
     */
    private final long total;

    /**
     * 전체 페이지 수
     * (total / size 를 올림 처리하여 계산)
     */
    private final int totalPages;

    /**
     * 생성자는 외부에서 직접 호출하지 못하도록 private 처리
     * → 정적 팩토리 메서드(of)를 통해서만 생성
     */
    private PageResultDTO(List<T> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    /**
     * PageResultDTO 생성용 정적 팩토리 메서드
     *
     * @param items 현재 페이지의 데이터 목록
     * @param page  현재 페이지 번호 (1부터 시작)
     * @param size  페이지당 데이터 개수
     * @param total 전체 데이터 개수
     * @param <T>   데이터 타입
     * @return PageResultDTO 인스턴스
     */
    public static <T> PageResultDTO<T> of(
            List<T> items,
            int page,
            int size,
            long total
    ) {
        return new PageResultDTO<>(items, page, size, total);
    }
}
