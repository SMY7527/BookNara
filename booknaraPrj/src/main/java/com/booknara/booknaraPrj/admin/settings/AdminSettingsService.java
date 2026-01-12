package com.booknara.booknaraPrj.admin.settings;

import com.booknara.booknaraPrj.admin.bookManagement.AdminBookIsbnRepository;
import com.booknara.booknaraPrj.admin.recomBooks.AdminBookSearchResponseDto;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomBooks;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomBooksRepository;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {
    private final AdminSettingsRepository adminSettingsRepository;
    private final AdminBookIsbnRepository bookIsbnRepository;
    private final AdminRecomBooksRepository adminRecomBooksRepository;

    @Transactional(readOnly = true)
    public AdminSettings getSettings() {
        // DB에 설정이 없으면 기본 객체를 반환하거나 에러를 방지함
        return adminSettingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new AdminSettings());
    }

    @Transactional
    public void updateSettings(AdminSettings newSettings) {
        AdminSettings existing = adminSettingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new AdminSettings());

        // 기존 데이터에 덮어쓰기
        existing.setDefaultLendDays(newSettings.getDefaultLendDays());
        existing.setDefaultExtendDays(newSettings.getDefaultExtendDays());
        existing.setLateFeePerDay(newSettings.getLateFeePerDay());
        existing.setLatePenaltyDays(newSettings.getLatePenaltyDays());
        existing.setMaxLendCount(newSettings.getMaxLendCount());
        existing.setDbUpdateCycleDays(newSettings.getDbUpdateCycleDays());
        existing.setAdminEmail(newSettings.getAdminEmail());
        existing.setAdminPhone(newSettings.getAdminPhone());

        adminSettingsRepository.save(existing);
    }

    // 도서 검색 (DTO 변환)
    @Transactional(readOnly = true)
    public Page<AdminBookSearchResponseDto> searchBooks(String keyword, Pageable pageable) {
        return bookIsbnRepository.findByBookTitleContainingOrIsbn13Containing(keyword, keyword, pageable)
                .map(book -> new AdminBookSearchResponseDto(book.getIsbn13(), book.getBookTitle(), book.getAuthors()));
    }

    // 랜덤 추출
    @Transactional(readOnly = true)
    public List<AdminBookSearchResponseDto> getRandomBooks(int count) {
        return bookIsbnRepository.findRandomBooks(count).stream()
                .map(book -> new AdminBookSearchResponseDto(book.getIsbn13(), book.getBookTitle(), book.getAuthors()))
                .toList();
    }

    // 추천 목록 최종 저장
    public void updateRecommendations(List<String> isbns) {
        adminRecomBooksRepository.deleteAllInBatch(); // 기존 목록 삭제

        List<AdminRecomBooks> newRecoms = isbns.stream()
                .map(isbn -> AdminRecomBooks.builder()
                        .isbn13(isbn)
                        .state(AdminRecomState.active) // 이전에 만든 테이블 설계 적용
                        .build())
                .toList();

        adminRecomBooksRepository.saveAll(newRecoms);
    }
}
