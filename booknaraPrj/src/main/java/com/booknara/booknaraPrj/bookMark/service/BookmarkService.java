package com.booknara.booknaraPrj.bookMark.service;

import com.booknara.booknaraPrj.bookMark.mapper.BookmarkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [BookmarkService]
 * 도서 북마크(찜하기) 기능을 관리하는 서비스입니다.
 * 사용자의 관심 도서 목록을 유지하고, 도서별 인기도(북마크 수) 통계를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    /**
     * [북마크 여부 확인]
     * 특정 사용자가 해당 도서를 북마크했는지 확인합니다.
     * @return 'Y'인 경우 true, 그 외(N 또는 데이터 없음) false
     */
    public boolean isBookmarked(String isbn13, String userId) {
        String yn = bookmarkMapper.selectBookmarkYn(isbn13, userId);
        return "Y".equalsIgnoreCase(yn);
    }

    /**
     * [북마크 토글(Toggle)]
     * 북마크 상태를 반전시킵니다. (등록 -> 해제 / 해제 -> 등록)
     * @return 변경 후의 북마크 상태 (true: 등록됨, false: 해제됨)
     */
    @Transactional
    public boolean toggle(String isbn13, String userId) {
        // 1. 현재 북마크 상태 조회
        String yn = bookmarkMapper.selectBookmarkYn(isbn13, userId);

        if ("Y".equalsIgnoreCase(yn)) {
            // 2-1. 이미 등록된 경우 -> 북마크 취소(N으로 변경 또는 삭제)
            bookmarkMapper.cancelBookmark(isbn13, userId);
            return false;
        }

        // 2-2. 등록되지 않았거나(N) 데이터가 없는(null) 경우 -> 북마크 등록(Y)
        // upsert 로직을 통해 기존 데이터가 있으면 Update, 없으면 Insert를 수행합니다.
        bookmarkMapper.upsertBookmark(isbn13, userId);
        return true;
    }

    /**
     * [도서별 북마크 총합 조회]
     * 특정 도서가 받은 총 북마크 수를 반환합니다.
     * 도서 상세 정보에서 인기도 지표로 활용됩니다.
     */
    public int countByIsbn(String isbn13) {
        return bookmarkMapper.countBookmarkByIsbn(isbn13);
    }
}