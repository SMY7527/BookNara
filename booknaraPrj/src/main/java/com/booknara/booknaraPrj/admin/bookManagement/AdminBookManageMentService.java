package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Page;

public interface AdminBookManageMentService {
    Page<AdminBookListResponseDto> getBookList(int page, String keyword, String bookState);
    void updateStatus(String isbn13, String bookState);
    void saveBookWithGenre(AdminBookSaveRequestDto dto);
}
