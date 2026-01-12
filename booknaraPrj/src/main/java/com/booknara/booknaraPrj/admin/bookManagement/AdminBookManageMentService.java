package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;

public interface AdminBookManageMentService {

    /**
     * [최적화] 대용량 조회를 위해 Slice 사용
     * keyword가 없을 때는 Slice로 빠르게 넘기고,
     * 특정 조건 검색 시에만 Page를 반환하도록 분리할 수도 있습니다.
     * 여기서는 일관성을 위해 Slice로 제안드립니다. (Page로 유지도 가능)
     */
    Slice<AdminBookListResponseDto> getBookList(String bookState, String keyword, Pageable pageable);

    void updateStatus(Long bookId, String bookState);

    void saveBookWithGenre(AdminBookSaveRequestDto dto);
}