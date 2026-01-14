package com.booknara.booknaraPrj.bookSearch.service;

import com.booknara.booknaraPrj.bookSearch.dto.BookSearchConditionDTO;
import com.booknara.booknaraPrj.bookSearch.dto.BookSearchDTO;
import com.booknara.booknaraPrj.bookSearch.dto.PageInsertDTO;
import com.booknara.booknaraPrj.bookSearch.mapper.BookSearchMapper;
import com.booknara.booknaraPrj.bookSearch.dto.PageResultDTO;
import com.booknara.booknaraPrj.security.uitil.LoginUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * [BookSearchService]
 * 사용자의 검색 요청을 분석하고 최적화하여 도서 목록을 반환하는 핵심 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookSearchMapper mapper;
    private final GenreService genreService;


    /**
     * 검색 조건과 페이징 정보를 기반으로 통합 검색을 수행합니다.
     * @param cond 검색어, 검색 필드, 정렬 기준 등
     * @param page 현재 페이지 및 페이지 크기
     * @return 검색된 도서 목록과 페이지 메타 정보
     */
    public PageResultDTO<BookSearchDTO> search(BookSearchConditionDTO cond, PageInsertDTO page) {

        // ✅ 0) Null 방어: 입력값이 비어있을 경우 기본 객체를 생성하여 NullPointerException 방지
        if (cond == null) cond = new BookSearchConditionDTO();
        if (page == null) page = new PageInsertDTO();

        // ✅ 1) 페이징 방어 (Clamping)
        // 최대 페이지 100만, 한 페이지당 최대 60건으로 제한하여 DB 과부하 방지
        int safePage = clamp(page.getPage(), 1, 1_000_000, 1);
        int safeSize = clamp(page.getSize(), 1, 60, 20);

        page.setPage(safePage);
        page.setSize(safeSize);
        // DB 시작 위치(Offset) 계산
        page.setOffset(Math.toIntExact(((long) safePage - 1) * safeSize));

        // ✅ 2) 입력값 화이트리스트 검증
        // 검색 필드, 정렬, 전자책 여부 등 정해진 값 이외의 입력은 기본값으로 강제 변환
        String field = upperOrNull(cond.getField());
        String sort = upperOrNull(cond.getSort());
        String ebook = upperOrNull(cond.getEbookYn());

        cond.setField(whitelistOrDefault(field, "ALL", "ALL", "TITLE", "AUTHOR", "PUBLISHER"));
        cond.setSort(whitelistOrDefault(sort, "NEW", "NEW", "RATING", "REVIEW"));
        cond.setEbookYn(whitelistOrDefault(ebook, "ALL", "ALL", "Y", "N"));

        // ✅ 3) 검색 키워드 가공 (Hybrid Search Strategy)
        String raw = cond.getKeyword();

        // LIKE 검색용: 공백 유지 및 공백 완전 제거 두 버전을 준비하여 검색 정확도 향상
        String likeKeyword = normalizeKeyword(raw);
        String nsKeyword   = removeAllSpaces(likeKeyword);

        // 전문 검색(FULLTEXT)용: '+단어*' 형태로 변환하여 고속 접두어 검색 수행
        String ftKeyword = normalizeFulltextKeyword(raw);
        String ftJoined  = normalizeFulltextJoined(raw);

        cond.setKeyword(likeKeyword);
        cond.setKeywordNs(nsKeyword);
        cond.setFtKeyword(ftKeyword);
        cond.setFtJoined(ftJoined);

        // 전문 검색 키워드가 성공적으로 생성되었을 경우에만 FULLTEXT 모드 활성화
        cond.setUseFulltext(cond.getFtKeyword() != null || cond.getFtJoined() != null);

        // ✅ 4) 외국도서 장르 특수 처리
        // 외국도서 전체 카테고리(-1) 선택 시 하위 장르들을 자동으로 주입하여 검색 범위 확장
        if ("외국도서".equals(cond.getMall())
                && cond.getParentGenreId() != null
                && cond.getParentGenreId() < 0
                && (cond.getForeignTopParentIds() == null || cond.getForeignTopParentIds().isEmpty())) {

            cond.setForeignTopParentIds(genreService.foreignTopParentIds(19, 0));
        }

        // 잘못된 장르 ID 값 보정
        if (cond.getGenreId() != null && cond.getGenreId() <= 0) cond.setGenreId(null);
        if (cond.getParentGenreId() != null) {
            Integer pid = cond.getParentGenreId();
            if (pid == 0 || pid < -1) cond.setParentGenreId(null);
        }

        // 로그인 사용자 정보 획득 (북마크, 대출 여부 등 개인화 데이터 조회를 위함)
        String userId = LoginUserUtils.getUserId();

        // 검색 총 건수 조회 및 목록 조회 수행
        long total = mapper.countBooks(cond, userId);
        List<BookSearchDTO> items = (total == 0) ? List.of() : mapper.searchBooks(cond, page, userId);

        // 최종 결과 포맷팅하여 반환
        return PageResultDTO.of(items, page.getPage(), page.getSize(), total);
    }


    /**
     * MySQL FULLTEXT(BOOLEAN MODE)를 위한 검색어 정규화
     * - 단어별로 +와 *를 붙여 AND 조건 및 접두어 매칭 수행 (예: "자바" -> "+자바*")
     */
    private static String normalizeFulltextKeyword(String keyword) {
        if (keyword == null) return null;

        String k = keyword.trim();
        if (k.isEmpty()) return null;

        // 보안 및 성능을 위한 길이 제한
        if (k.length() > 100) k = k.substring(0, 100);
        k = k.replaceAll("\\s{2,}", " ");

        // 사용자가 이미 BOOLEAN 연산자를 직접 쓴 경우는 원문 유지
        if (containsBooleanOperator(k)) {
            return k;
        }

        String[] tokens = k.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String t : tokens) {
            String term = sanitizeToken(t);
            if (term.isEmpty()) continue;

            // 2글자 이상의 토큰만 전문 검색 인덱스 활용 (MySQL 기본 설정 대응)
            if (term.length() < 2) continue;

            if (sb.length() > 0) sb.append(' ');
            sb.append('+').append(term).append('*');
        }

        String out = sb.toString().trim();
        return out.isEmpty() ? null : out;
    }

    /** LIKE 검색을 위한 일반 검색어 정규화 (공백 정리) */
    private static String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.isEmpty()) return null;
        if (k.length() > 100) k = k.substring(0, 100);
        k = k.replaceAll("\\s{2,}", " ");
        return k;
    }

    /** 띄어쓰기 없는 검색을 지원하기 위해 모든 공백 제거 */
    private static String removeAllSpaces(String s){
        if (s == null) return null;
        String t = s.replaceAll("\\s+", "");
        return t.isEmpty() ? null : t;
    }

    /** 검색어 전체를 하나의 토큰으로 붙여 전문 검색 수행 (예: "해리 포터" -> "+해리포터*") */
    private static String normalizeFulltextJoined(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.isEmpty()) return null;
        if (k.length() > 100) k = k.substring(0, 100);
        k = k.replaceAll("\\s{2,}", " ");

        if (containsBooleanOperator(k)) return null;

        String joined = k.replaceAll("\\s+", "");
        joined = sanitizeToken(joined);
        if (joined.length() < 2) return null;

        return "+" + joined + "*";
    }


    /** BOOLEAN MODE 특수 연산자 포함 여부 확인 */
    private static boolean containsBooleanOperator(String s) {
        return s.matches(".*[\\+\\-\\\"\\*\\(\\)<>~@].*");
    }


    /** 전문 검색용 토큰 정제: 한글/영문/숫자/_를 제외한 특수문자 제거 */
    private static String sanitizeToken(String token) {
        if (token == null) return "";
        return token.replaceAll("[^0-9A-Za-z가-힣_]", "");
    }


    /**
     * 허용된 값(Whitelist)인지 확인하고 아닐 경우 기본값 반환
     * SQL Injection 방어 및 시스템 안정성 확보를 위함
     */
    private static String whitelistOrDefault(String value, String defaultValue, String... allowed) {
        if (value == null) return defaultValue;
        for (String a : allowed) {
            if (a.equals(value)) return value;
        }
        return defaultValue;
    }

    /** 입력된 수치를 허용 범위 내로 고정(Clamp) */
    private static int clamp(int v, int min, int max, int defaultValue) {
        if (v < min) return defaultValue;
        return Math.min(v, max);
    }

    /** 문자열을 대문자로 변환하고 공백 제거, 빈 문자열은 null로 반환 */
    private static String upperOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        return t.toUpperCase();
    }
}