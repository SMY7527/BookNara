package com.booknara.booknaraPrj.bookMark.service;

import com.booknara.booknaraPrj.bookMark.mapper.BookmarkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    public boolean isBookmarked(String isbn13, String userId) {
        String yn = bookmarkMapper.selectBookmarkYn(isbn13, userId);
        return "Y".equalsIgnoreCase(yn);
    }

    @Transactional
    public boolean toggle(String isbn13, String userId) {
        String yn = bookmarkMapper.selectBookmarkYn(isbn13, userId);

        if ("Y".equalsIgnoreCase(yn)) {
            bookmarkMapper.cancelBookmark(isbn13, userId);
            return false; // 해제됨
        }

        // N이거나 null이면 -> Y로
        bookmarkMapper.upsertBookmark(isbn13, userId);
        return true; // 등록됨
    }

    public int countByIsbn(String isbn13) {
        return bookmarkMapper.countBookmarkByIsbn(isbn13);
    }
}
