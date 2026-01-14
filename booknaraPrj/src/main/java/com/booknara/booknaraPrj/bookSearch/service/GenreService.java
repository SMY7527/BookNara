package com.booknara.booknaraPrj.bookSearch.service;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import com.booknara.booknaraPrj.bookSearch.mapper.GenreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [GenreService]
 * 도서 카테고리(장르)의 계층 구조를 관리하고, 검색 필터용 동적 데이터를 생성하는 서비스입니다.
 * * 설계 핵심:
 * 1) 동적 카테고리: 실제 도서가 있는 장르만 사용자에게 노출하여 빈 검색 결과 방지
 * 2) 가상 노드 처리: '기타(-1)', '외국도서' 등 DB에 없는 UI 전용 카테고리 관리
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreMapper mapper;

    /**
     * [국내도서] 인기 상위 부모 장르 자동 추출
     * - 도서 보유량이 기준(min) 이상인 상위 카테고리만 선별
     * - 메인 화면의 좌측 카테고리 메뉴를 구성하는 기초 데이터
     */
    public List<GenreDTO> parentsAuto(Integer top, Integer min) {
        // 기본값 설정: 상위 20개, 최소 0권 이상
        int t = (top == null || top <= 0) ? 20 : top;
        int m = (min == null || min < 0) ? 0 : min;
        return mapper.selectParentGenresAuto(t, m);
    }

    /**
     * [국내도서] 특정 부모 아래의 하위(자식) 장르 조회
     * - 부모 장르 마우스 오버/클릭 시 노출될 2차 카테고리 목록
     * - 실제 매핑된 도서가 1권이라도 있는 장르만 반환하여 UX 품질 유지
     */
    public List<GenreDTO> childrenAuto(Integer parentId, Integer top, Integer min) {
        if (parentId == null || parentId <= 0) return List.of();

        int t = (top == null || top <= 0) ? 30 : top;
        int m = (min == null || min < 0) ? 0 : min;

        return mapper.selectChildGenresAuto(parentId, t, m);
    }

    /**
     * [외국도서] '기타' 필터 처리를 위한 상위 ID 목록 추출
     * - 외국도서 중 가장 비중이 높은 Top N 장르의 ID만 추출
     * - BookSearchService에서 '기타(-1)' 선택 시, 이 ID들을 제외(NOT IN)하고
     * 검색하기 위한 비교군 데이터로 활용됨
     */
    public List<Integer> foreignTopParentIds(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 19 : top;
        int m = (min == null || min < 0) ? 0 : min;

        return mapper.selectForeignParentGenresAuto(t, m).stream()
                .map(GenreDTO::getGenreId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
    }

    /**
     * [UI 전용] 국내도서 상위 목록에 '외국도서' 진입점 추가
     * - 전체 도서 분류의 최상위 메뉴 구성을 위해 사용
     * - '외국도서'는 실제 장르 테이블에 없으므로 가상 DTO를 생성하여 리스트에 주입
     */
    public List<GenreDTO> parentsAutoWithForeign(Integer top, Integer min) {
        List<GenreDTO> list = parentsAuto(top, min);

        // 가상 노드 생성: 외국도서 전체 진입용
        GenreDTO foreign = new GenreDTO();
        foreign.setGenreId(null);
        foreign.setGenreNm("외국도서");
        foreign.setParentId(null);
        foreign.setMall("외국도서");

        list.add(foreign);
        return list;
    }

    /**
     * [UI 전용] 외국도서 상위 목록 + '기타(-1)' 가상 카테고리 구성
     * - 외국도서는 장르가 너무 방대하므로 Top N개만 노출하고 나머지는 '기타'로 묶어 처리
     * - -1 이라는 가상 ID를 부여하여 UI에서 선택 가능하게 만듦
     */

    public List<GenreDTO> foreignParentsTopWithEtc(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 19 : top;
        int m = (min == null || min < 0) ? 0 : min;

        List<GenreDTO> list = mapper.selectForeignParentGenresAuto(t, m);

        // 가상 노드 생성: 나머지 장르를 묶어줄 '기타' 카테고리
        GenreDTO etc = new GenreDTO();
        etc.setGenreId(-1);
        etc.setGenreNm("기타");
        etc.setParentId(null);
        etc.setMall("외국도서");

        list.add(etc);
        return list;
    }
}