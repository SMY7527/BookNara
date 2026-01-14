package com.booknara.booknaraPrj.bookAPI.service.policy;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

/**
 * [TempReadyPolicy]
 * TEMP 테이블의 도서 데이터를 운영 테이블(BOOK_ISBN)로 이관하기 위한
 * 품질 기준(READY 상태 승격 조건)을 정의하는 정책 컴포넌트입니다.
 */
@Component
public class TempReadyPolicy {

    /**
     * READY(1) 상태로 승격 가능한지 여부를 최종 판정합니다.
     * 1) 기본 메타데이터 존재 여부
     * 2) 서비스 전시용 품질 요건 충족 여부
     */
    public boolean isReady(BookIsbnTempDTO tempBook) {
        if (tempBook == null) return false;

        // [핵심 조건] 장르 정보는 필수이며, 반드시 유효한 ID(>0)여야 함
        Integer genreId = tempBook.getGenreId();
        if (genreId == null || genreId <= 0) return false;

        // 1) 필수 메타데이터 체크 (ISBN, 제목, 출판사)
        boolean hasRequiredMetadata = hasRequiredMetadata(tempBook);

        // 2) 품질 요건 체크 (저자, 출간일, 장르, 알라딘 고화질 표지)
        boolean hasQualityContent = hasQualityContent(tempBook);

        return hasRequiredMetadata && hasQualityContent;
    }

    /**
     * 시스템 운영을 위한 최소한의 식별 정보 존재 여부를 확인합니다.
     * 대상: ISBN / 제목 / 출판사
     */
    private boolean hasRequiredMetadata(BookIsbnTempDTO tempBook) {
        return isNotBlank(tempBook.getIsbn13())
                && isNotBlank(tempBook.getBookTitle())
                && isNotBlank(tempBook.getPublisher());
    }

    /**
     * 사용자에게 노출될 정보의 품질을 확인합니다.
     * 대상: 저자 / 출간일 / 장르 / 알라딘 표지 이미지(Big)
     * * [비즈니스 판단]
     * - 네이버 표지는 품질이 일정하지 않아 '알라딘 표지'만 필수 요건으로 간주합니다.
     * - 네이버 응답 상태(NAVER_RES_STATUS)는 과거 데이터 호환성을 위해 체크 대상에서 제외합니다.
     */
    private boolean hasQualityContent(BookIsbnTempDTO tempBook) {
        boolean hasAuthors = isNotBlank(tempBook.getAuthors());
        boolean hasPubdate = tempBook.getPubdate() != null;

        // 장르 ID 유효성 재검증
        boolean hasGenreId = tempBook.getGenreId() != null && tempBook.getGenreId() > 0;

        // ✅ 알라딘 고화질 표지(Big) 존재 여부가 승격의 핵심 지표
        boolean hasAladinCover = isNotBlank(tempBook.getAladinImageBig());

        return hasAuthors && hasPubdate && hasGenreId && hasAladinCover;
    }

    /** 문자열의 Null 및 공백 여부를 안전하게 검사합니다. */
    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}