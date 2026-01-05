package com.booknara.booknaraPrj.admin.bookMangement;

import org.springframework.data.domain.Page;

public interface BookManageMentService {
    Page<BookListResponseDto> getBookList(int page);
    Page<BookListResponseDto> getBookList(int page, String keyword);
}
