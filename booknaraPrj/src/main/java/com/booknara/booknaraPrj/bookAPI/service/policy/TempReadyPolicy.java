package com.booknara.booknaraPrj.bookAPI.service.policy;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

@Component
public class TempReadyPolicy {

    /**
     * READY(1) 승격 조건
     * - 필수 메타: ISBN / 제목 / 출판사
     * - 품질 요건: 저자 / 출간일 / 장르(>0) / 알라딘 표지(Big)
     *
     * 주의)
     * - NAVER_RES_STATUS는 과거 데이터(도입 이전) 때문에 0이 많아 READY 조건에서 제외
     * - 표지는 네이버가 아니라 "알라딘 표지"만 필수로 본다
     */
    public boolean isReady(BookIsbnTempDTO tempBook) {
        if (tempBook == null) return false;

        // ✅ 장르는 반드시 정상값(>0)
        Integer genreId = tempBook.getGenreId();
        if (genreId == null || genreId <= 0) return false;

        // 1) 필수 메타
        boolean hasRequiredMetadata = hasRequiredMetadata(tempBook);

        // 2) 품질 요건
        boolean hasQualityContent = hasQualityContent(tempBook);

        return hasRequiredMetadata && hasQualityContent;
    }

    /** ISBN / 제목 / 출판사 존재 여부 */
    private boolean hasRequiredMetadata(BookIsbnTempDTO tempBook) {
        return isNotBlank(tempBook.getIsbn13())
                && isNotBlank(tempBook.getBookTitle())
                && isNotBlank(tempBook.getPublisher());
    }

    /**
     * 저자 / 출간일 / 장르 / 알라딘 표지 이미지(Big)
     * - 네이버 표지는 고려하지 않음(덤)
     */
    private boolean hasQualityContent(BookIsbnTempDTO tempBook) {
        boolean hasAuthors = isNotBlank(tempBook.getAuthors());
        boolean hasPubdate = tempBook.getPubdate() != null;

        // genreId는 isReady()에서 이미 검증했지만, 의미 명확하게 한 번 더 체크해도 됨
        boolean hasGenreId = tempBook.getGenreId() != null && tempBook.getGenreId() > 0;

        // ✅ 알라딘 표지(Big)만 필수
        boolean hasAladinCover = isNotBlank(tempBook.getAladinImageBig());

        return hasAuthors && hasPubdate && hasGenreId && hasAladinCover;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
