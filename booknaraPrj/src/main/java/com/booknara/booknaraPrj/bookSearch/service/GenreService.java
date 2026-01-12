package com.booknara.booknaraPrj.bookSearch.service;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import com.booknara.booknaraPrj.bookSearch.mapper.GenreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 장르 서비스
 *
 * <p>
 * 이 서비스는 "장르 메뉴 구성"과 "검색 조건용 장르 데이터"를 제공한다.
 *
 * 핵심 설계 원칙:
 * 1) 장르 통계 계산(Top N 등)은 여기서만 수행한다.
 * 2) BookSearch 쿼리는 이미 계산된 장르 결과를 "필터 조건"으로만 사용한다.
 * 3) '기타(-1)'는 UI/UX를 위한 가상 카테고리이며 DB에는 존재하지 않는다.
 *
 * 즉,
 * - GenreService = 카테고리 정의 / 계산 담당
 * - BookSearchService = 검색 조건 해석 및 전달 담당
 * - Mapper = DB 접근만 담당
 * </p>
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreMapper mapper;

    /**
     * 국내도서 상위 부모 장르 자동 추출
     *
     * <p>
     * BOOK_ISBN에 실제로 매핑된 권수가 일정 기준(min) 이상인
     * "국내도서 부모 장르"만 집계하여 반환한다.
     *
     * - 화면 좌측 카테고리 메뉴 구성에 사용
     * - 검색 쿼리에서는 사용하지 않음
     * </p>
     */
    public List<GenreDTO> parentsAuto(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 20 : top;
        int m = (min == null || min < 0) ? 0 : min;
        return mapper.selectParentGenresAuto(t, m);
    }

    /**
     * 국내도서 특정 부모 장르의 하위(자식) 장르 조회
     *
     * <p>
     * - 실제 도서가 1권 이상 존재하는 자식 장르만 반환
     * - 부모 장르 클릭 시 우측(또는 하위) 카테고리 목록 구성용
     * </p>
     */
    public List<GenreDTO> childrenAuto(Integer parentId, Integer top, Integer min) {
        if (parentId == null || parentId <= 0) return List.of();

        int t = (top == null || top <= 0) ? 30 : top;
        int m = (min == null || min < 0) ? 0 : min;

        return mapper.selectChildGenresAuto(parentId, t, m);
    }

    /**
     * 외국도서 Top 부모 장르 ID 목록 반환 (검색 조건 전용)
     *
     * <p>
     * 이 메서드는 "기타(-1)" 검색 처리를 위해 존재한다.
     *
     * - 외국도서에서 사용 빈도가 높은 상위 부모 장르 Top N을 추출
     * - 반환값은 GenreDTO가 아닌 "GENRE_ID 목록"만 제공
     * - '기타(-1)'는 포함하지 않음
     *
     * 사용처:
     * - BookSearchService에서 parentGenreId = -1(기타)일 때
     *   → Top N을 제외한 나머지 장르를 검색하기 위한 NOT IN 조건 생성용
     *
     * 주의:
     * - 이 메서드는 "카테고리 계산"용이며
     * - BookSearchMapper 내부에서 재계산하면 안 된다.
     * </p>
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
     * 국내도서 상위 장르 목록 + '외국도서' 진입용 가상 카테고리 추가
     *
     * <p>
     * - 화면 상단/좌측의 "국내도서 / 외국도서" 전환용 메뉴 구성
     * - '외국도서' 항목은 실제 GENRE 테이블에 존재하지 않는 가상 노드
     * </p>
     */
    public List<GenreDTO> parentsAutoWithForeign(Integer top, Integer min) {
        List<GenreDTO> list = parentsAuto(top, min);

        GenreDTO foreign = new GenreDTO();
        foreign.setGenreId(null);
        foreign.setGenreNm("외국도서");
        foreign.setParentId(null);
        foreign.setMall("외국도서");

        list.add(foreign);
        return list;
    }

    /**
     * 외국도서 부모 장르 Top N + '기타(-1)' 항목 반환 (카테고리 메뉴 전용)
     *
     * <p>
     * - Top N 부모 장르 + '기타'를 합쳐 총 N+1개 반환
     * - '기타(-1)'는 UI 전용 가상 카테고리
     *
     * 사용처:
     * - 외국도서 카테고리 메뉴 구성
     *
     * 주의:
     * - 검색 쿼리에서는 이 메서드를 그대로 사용하지 않는다.
     * - 검색 시에는 {@link #foreignTopParentIds(Integer, Integer)} 결과를 사용한다.
     * </p>
     */
    public List<GenreDTO> foreignParentsTopWithEtc(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 19 : top;
        int m = (min == null || min < 0) ? 0 : min;

        List<GenreDTO> list = mapper.selectForeignParentGenresAuto(t, m);

        GenreDTO etc = new GenreDTO();
        etc.setGenreId(-1);               // '기타'는 DB에 없는 가상 카테고리
        etc.setGenreNm("기타");
        etc.setParentId(null);
        etc.setMall("외국도서");

        list.add(etc);
        return list;
    }
}
