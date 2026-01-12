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
 * 도서 검색 서비스
 *
 * <p>
 * - 사용자 입력(검색어/필터/정렬/페이징)을 받아 "안전한 값"으로 정규화(기본값/상한선/화이트리스트)한 뒤
 *   MyBatis Mapper를 통해 실제 검색 결과를 조회한다.
 * - 입력값 검증의 목적은 SQL 인젝션(#{ 바인딩으로 상당부분 방어됨)보다는
 *   "이상값/폭탄 요청(size/page)"에 의해 DB가 과부하 되는 것을 방지하고,
 *   화면이 항상 일관된 기본 동작(NEW 정렬, ALL 검색 등)을 하게 만드는 것이다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookSearchMapper mapper;
    private final GenreService genreService;


    /**
     * 검색 조건 + 페이징을 기반으로 도서 목록을 조회하고, 페이지 메타 정보와 함께 반환한다.
     *
     * @param cond 검색 조건(검색어, 필드, 카테고리, 전자책 필터, 정렬)
     * @param page 페이징 정보(page, size) + 내부 계산용 offset
     * @return PageResultDTO(목록 + 페이지 정보)
     */
    public PageResultDTO<BookSearchDTO> search(BookSearchConditionDTO cond, PageInsertDTO page) {

        // ✅ 0) null 방어 먼저
        if (cond == null) cond = new BookSearchConditionDTO();
        if (page == null) page = new PageInsertDTO();

        // ✅ 1) 페이징 방어
        int safePage = clamp(page.getPage(), 1, 1_000_000, 1);
        int safeSize = clamp(page.getSize(), 1, 60, 20);

        page.setPage(safePage);
        page.setSize(safeSize);
        page.setOffset(Math.toIntExact(((long) safePage - 1) * safeSize));

        // ✅ 2) field/sort/ebookYn 화이트리스트 (keyword보다 먼저 해도 됨)
        String field = upperOrNull(cond.getField());
        String sort = upperOrNull(cond.getSort());
        String ebook = upperOrNull(cond.getEbookYn());

        cond.setField(whitelistOrDefault(field, "ALL", "ALL", "TITLE", "AUTHOR", "PUBLISHER"));
        cond.setSort(whitelistOrDefault(sort, "NEW", "NEW", "RATING", "REVIEW"));
        cond.setEbookYn(whitelistOrDefault(ebook, "ALL", "ALL", "Y", "N"));

        // ✅ 3) keyword: FULLTEXT 시도 → 실패하면 LIKE 폴백
        String raw = cond.getKeyword();

        String likeKeyword = normalizeKeyword(raw);          // "치 킨" -> "치 킨"
        String nsKeyword   = removeAllSpaces(likeKeyword);   // "치 킨" -> "치킨"

        String ftKeyword = normalizeFulltextKeyword(raw);    // "치 킨" -> "+치* +킨*"
        String ftJoined  = normalizeFulltextJoined(raw);     // "치 킨" -> "+치킨*"

        cond.setKeyword(likeKeyword);
        cond.setKeywordNs(nsKeyword);

        cond.setFtKeyword(ftKeyword);
        cond.setFtJoined(ftJoined);
        // ftKeyword가 만들어지면 FULLTEXT 사용
        cond.setUseFulltext(cond.getFtKeyword() != null || cond.getFtJoined() != null);

        // ✅ 4) 외국도서 + 기타(-1)일 때 Top19 부모 장르 ID 주입
        if ("외국도서".equals(cond.getMall())
                && cond.getParentGenreId() != null
                && cond.getParentGenreId() < 0
                && (cond.getForeignTopParentIds() == null || cond.getForeignTopParentIds().isEmpty())) {

            cond.setForeignTopParentIds(genreService.foreignTopParentIds(19, 0)); // min은 너 정책
        }

        if (cond.getGenreId() != null && cond.getGenreId() <= 0) cond.setGenreId(null);
        if (cond.getParentGenreId() != null) {
            Integer pid = cond.getParentGenreId();
            if (pid == 0 || pid < -1) cond.setParentGenreId(null);
        }

        String userId = LoginUserUtils.getUserId();
        long total = mapper.countBooks(cond, userId);
        List<BookSearchDTO> items = (total == 0) ? List.of() : mapper.searchBooks(cond, page, userId);
        return PageResultDTO.of(items, page.getPage(), page.getSize(), total);
    }


    /**
     * FULLTEXT(BOOLEAN MODE) 검색어 정규화
     * - null/공백 -> null
     * - 길이 제한
     * - 연속 공백 축소
     * - 단어 단위로 +word* 형태로 변환 (AND + prefix match)
     *
     * 주의:
     * - MySQL FULLTEXT는 "토큰" 기준이라 LIKE '%...%'와 결과가 다를 수 있음
     * - 1~2글자 토큰은 설정(ft_min_word_len / innodb_ft_min_token_size)에 따라 검색이 안 될 수 있음
     */
    private static String normalizeFulltextKeyword(String keyword) {
        if (keyword == null) return null;

        String k = keyword.trim();
        if (k.isEmpty()) return null;

        // 너무 긴 검색어 제한(성능/로그)
        if (k.length() > 100) k = k.substring(0, 100);

        // 연속 공백 정리
        k = k.replaceAll("\\s{2,}", " ");

        // 사용자가 BOOLEAN MODE 연산자를 의도적으로 쓴 경우는 최대한 존중
        // (예: +"자바 스프링" -오라클, spring* 등)
        if (containsBooleanOperator(k)) {
            return k;
        }

        // 단어 분리 후: +term* 로 변환 (단, 너무 짧은 토큰은 제외)
        String[] tokens = k.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String t : tokens) {
            String term = sanitizeToken(t);
            if (term.isEmpty()) continue;

            // 너무 짧은 단어는 FULLTEXT에서 매칭 안 될 가능성이 높아서 제외(옵션)
            // 2글자도 환경에 따라 안 될 수 있음. 기본은 2로 둠.
            if (term.length() < 2) continue;

            if (sb.length() > 0) sb.append(' ');
            sb.append('+').append(term).append('*');
        }

        String out = sb.toString().trim();
        return out.isEmpty() ? null : out;
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) return null;

        String k = keyword.trim();
        if (k.isEmpty()) return null;

        if (k.length() > 100) k = k.substring(0, 100);

        // 연속 공백 정리
        k = k.replaceAll("\\s{2,}", " ");

        return k;
    }

    private static String removeAllSpaces(String s){
        if (s == null) return null;
        String t = s.replaceAll("\\s+", "");
        return t.isEmpty() ? null : t;
    }

    private static String normalizeFulltextJoined(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.isEmpty()) return null;
        if (k.length() > 100) k = k.substring(0, 100);
        k = k.replaceAll("\\s{2,}", " ");

        // 사용자가 BOOLEAN 연산자 직접 쓴 경우는 건드리지 않음
        if (containsBooleanOperator(k)) return null;

        // 토큰 붙이기
        String joined = k.replaceAll("\\s+", "");
        joined = sanitizeToken(joined);
        if (joined.length() < 2) return null;

        return "+" + joined + "*";
    }


    /** BOOLEAN MODE 연산자/구문이 포함되면 사용자가 직접 쓴 것으로 간주 */
    private static boolean containsBooleanOperator(String s) {
        // + - " * () < > ~ @ 같은 토큰이 들어가면 변환하지 않음(보수적)
        // * 는 일반 검색에도 들어갈 수 있지만 BOOLEAN에서 의미가 있어서 포함.
        return s.matches(".*[\\+\\-\\\"\\*\\(\\)<>~@].*");
    }



    /** FULLTEXT용으로 토큰을 정리: 특수문자 제거(한글/영문/숫자/_만 허용) */
    private static String sanitizeToken(String token) {
        if (token == null) return "";
        // 한글, 영문, 숫자, 밑줄만 남김
        return token.replaceAll("[^0-9A-Za-z가-힣_]", "");
    }


    /**
     * 화이트리스트 기반 값 검증:
     * - value가 allowed 목록 중 하나면 value 반환
     * - 아니면 defaultValue 반환
     *
     * <p>주의: 절대 `${}` 같은 문자열 치환 SQL에 이 값을 그대로 쓰지 말 것.
     * 지금 구조처럼 MyBatis `<choose>` / `#{}` 바인딩과 함께 쓰면 안전하다.</p>
     */
    private static String whitelistOrDefault(String value, String defaultValue, String... allowed) {
        if (value == null) return defaultValue;
        for (String a : allowed) {
            if (a.equals(value)) return value;
        }
        return defaultValue;
    }

    /**
     * 숫자 입력값 보정(클램프):
     * - min 미만이면 defaultValue로 보정
     * - max 초과면 max로 제한
     * - 정상 범위면 그대로 반환
     * @param v            입력값
     * @param min          허용 최소값
     * @param max          허용 최대값
     * @param defaultValue min 미만일 때 적용할 기본값
     */
    private static int clamp(int v, int min, int max, int defaultValue) {
        if (v < min) return defaultValue;
        return Math.min(v, max);
    }

    /**
     * 문자열을 대문자로 변환(입력값 통일)
     * - null이면 null
     * - 공백만이면 null
     */
    private static String upperOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        return t.toUpperCase();
    }
}
