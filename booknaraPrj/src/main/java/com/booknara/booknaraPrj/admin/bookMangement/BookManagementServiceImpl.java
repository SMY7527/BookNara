package com.booknara.booknaraPrj.admin.bookMangement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookManagementServiceImpl implements BookManageMentService {
    private final BookManagementRepository bookManagementRepository;

    @Override
    public Page<BookListResponseDto> getBookList(int page) {
        // 한 페이지당 50개씩 조회
        Pageable pageable = PageRequest.of(page, 50);
        return bookManagementRepository.findAllBookManagementList(pageable);
    }

    @Override
    public Page<BookListResponseDto> getBookList(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 50);
        if (keyword == null || keyword.isEmpty()) {
            return bookManagementRepository.findAllBookManagementList(pageable);
        }
        return bookManagementRepository.findByKeyword(keyword, pageable);
    }
}