package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Getter;
import java.util.List;

/**
 * [PageResultDTO]
 * 검색 결과나 도서 목록 등 페이지네이션이 필요한 모든 API의 공통 응답 포맷입니다.
 * 제네릭(<T>)을 사용하여 어떤 도메인 객체(도서, 리뷰, 북마크 등)든 담을 수 있는 범용성을 갖췄습니다.
 */
@Getter
public class PageResultDTO<T> {

    /** 실제 조회된 데이터 리스트 (예: List<BookSearchDTO>) */
    private final List<T> items;

    /** 사용자가 현재 보고 있는 페이지 번호 (1-based) */
    private final int page;

    /** 한 페이지에 노출하기로 설정된 데이터의 양 */
    private final int size;

    /** 검색 조건에 부합하는 전체 데이터의 총 개수 (DB의 COUNT 결과) */
    private final long total;

    /** * [핵심 로직] 전체 페이지 수
     * 전체 건수(total)를 페이지 크기(size)로 나눈 후 올림(Math.ceil) 처리하여
     * 마지막 페이지 번호를 자동으로 산출합니다.
     */
    private final int totalPages;

    /**
     * 내부 생성자: 직접적인 인스턴스화를 방지하고 필드값의 무결성을 유지합니다.
     * 올림 계산 시 데이터 손실 방지를 위해 (double) 캐스팅을 활용합니다.
     */
    private PageResultDTO(List<T> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    /**
     * [정적 팩토리 메서드]
     * 'new' 키워드 대신 'of' 메서드를 통해 객체를 생성함으로써 가독성을 높이고
     * 생성 로직을 캡슐화합니다.
     *
     * @param items 현재 페이지 데이터
     * @param page  현재 페이지 번호
     * @param size  페이지당 건수
     * @param total 전체 건수
     * @return 계산된 페이지 메타 정보가 포함된 PageResultDTO
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