package com.booknara.booknaraPrj.service.policy;

import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

@Component
public class TempReadyPolicy {

    /**
     * READY(1) 승격 조건
     * - 필수 메타: ISBN / 제목 / 출판사
     * - 품질 요건: 저자 / 출간일 / 표지 이미지(네이버 or 알라딘 중 하나)
     *  검색 페이지 최소 기준
     */
    public boolean isReady(BookIsbnTempDTO tempBook) {
        if (tempBook == null) {
            return false;
        }

        boolean hasRequiredMetadata = hasRequiredMetadata(tempBook);
        boolean hasQualityContent   = hasQualityContent(tempBook);

        return hasRequiredMetadata && hasQualityContent;
    }

    /** ISBN / 제목 / 출판사 존재 여부 */
    private boolean hasRequiredMetadata(BookIsbnTempDTO tempBook) {
        return isNotBlank(tempBook.getIsbn13())
                && isNotBlank(tempBook.getBookTitle())
                && isNotBlank(tempBook.getPublisher());
    }

    /** 저자 / 출간일 / 장르/ 표지 이미지 중 최소 조건 충족 여부 */
    private boolean hasQualityContent(BookIsbnTempDTO tempBook) {
        boolean hasAuthors = isNotBlank(tempBook.getAuthors());
        boolean hasPubdate = tempBook.getPubdate() != null;
        boolean hasGenreId = tempBook.getGenreId() != null;   // ✅ 핵심 추가
        boolean hasCover   = hasCoverImage(tempBook);

        return hasAuthors && hasPubdate && hasGenreId && hasCover;
    }


    /** 네이버 또는 알라딘 표지 이미지 존재 */
    private boolean hasCoverImage(BookIsbnTempDTO tempBook) {
        return isNotBlank(tempBook.getNaverImage())
                || isNotBlank(tempBook.getAladinImageBig());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
