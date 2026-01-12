package com.booknara.booknaraPrj.admin.settings;

import com.booknara.booknaraPrj.admin.bookManagement.AdminBookIsbn;
import com.booknara.booknaraPrj.admin.bookManagement.AdminBookIsbnRepository;
import com.booknara.booknaraPrj.admin.recomBooks.AdminBookSearchResponseDto;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomBooks;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomBooksRepository;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {
    private final AdminSettingsRepository adminSettingsRepository;
    private final AdminBookIsbnRepository adminBookIsbnRepository;
    private final AdminRecomBooksRepository adminRecomBooksRepository;

    @Transactional(readOnly = true)
    public AdminSettings getSettings() {
        return adminSettingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new AdminSettings());
    }

    @Transactional
    public void updateSettings(AdminSettings newSettings) {
        AdminSettings existing = adminSettingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new AdminSettings());

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

    // 1. 도서 검색 (검색어 기반)
    @Transactional(readOnly = true)
    public Page<AdminBookSearchResponseDto> searchBooks(String keyword, Pageable pageable) {
        return adminBookIsbnRepository.findByBookTitleContainingOrIsbn13Containing(keyword, keyword, pageable)
                .map(book -> new AdminBookSearchResponseDto(
                        book.getIsbn13(),
                        book.getBookTitle(),
                        book.getAuthors()
                ));
    }

    // 2. 랜덤 추출 로직 (Offset 방식)
    public List<AdminBookSearchResponseDto> getRandomBooks(int count) {
        long total = adminBookIsbnRepository.countByEBookYnNative();
        if (total == 0) return new ArrayList<>();

        int maxOffset = (int) Math.max(0, total - count);
        int randomOffset = (int) (Math.random() * (maxOffset + 1));

        List<AdminBookIsbn> entities = adminBookIsbnRepository.findRandomBooksWithOffset(count, randomOffset);

        return entities.stream()
                .map(book -> new AdminBookSearchResponseDto(
                        book.getIsbn13(),
                        book.getBookTitle(),
                        book.getAuthors()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 3. 추천 목록 저장 로직 (중요 수정)
     * - DTO가 아닌 AdminRecomBooks 엔티티를 생성하여 저장합니다.
     */
// 추천 목록 저장 로직 수정
    @Transactional
    public void saveRandomRecomBooks(List<String> isbns) {
        // [수정] 여기서 모든 항목을 inactive로 만드는 로직이 있었다면 삭제해야 합니다.

        for (String isbn : isbns) {
            // 1. 이미 추천 도서 테이블에 존재하는지 확인
            Optional<AdminRecomBooks> existing = adminRecomBooksRepository.findByIsbn13(isbn);

            if (existing.isPresent()) {
                // 2. 이미 존재한다면 상태만 다시 active로 확실히 변경 (기존 데이터 유지)
                existing.get().setState(AdminRecomState.active);
            } else {
                // 3. 테이블에 아예 없던 도서라면 새로 추가
                AdminRecomBooks newRecom = AdminRecomBooks.builder()
                        .isbn13(isbn)
                        .state(AdminRecomState.active)
                        .build();
                adminRecomBooksRepository.save(newRecom);
            }
        }
    }

    // 4. 기존 추천 도서 전체 교체 (업데이트 시 사용)
    @Transactional
    public void updateRecommendations(List<String> newIsbns) {

        // 새로운 ISBN 리스트 처리 (위의 로직 재사용 가능하므로 로직 통합 가능)
        saveRandomRecomBooks(newIsbns);
    }

    // 5. 추천 도서 활성화 목록 조회 (Join 효과)
    @Transactional(readOnly = true)
    public List<AdminBookSearchResponseDto> getActiveRecommendations() {
        List<AdminRecomBooks> activeRecoms = adminRecomBooksRepository.findByState(AdminRecomState.active);

        return activeRecoms.stream()
                .map(recom -> {
                    // findByIsbn13을 사용하여 상세 정보 매핑
                    return adminBookIsbnRepository.findByIsbn13(recom.getIsbn13())
                            .map(book -> new AdminBookSearchResponseDto(book.getIsbn13(), book.getBookTitle(), book.getAuthors()))
                            .orElseGet(() -> new AdminBookSearchResponseDto(recom.getIsbn13(), "삭제된 도서", "알 수 없음"));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateRecommendation(String isbn) {
        // ISBN으로 해당 추천 도서를 찾아서 상태를 변경합니다.
        adminRecomBooksRepository.findByIsbn13(isbn).ifPresent(recom -> {
            recom.setState(AdminRecomState.inactive);
            // JPA의 변경 감지로 인해 트랜잭션 종료 시 자동 업데이트되지만,
            // 확실히 하기 위해 save를 명시할 수 있습니다.
            adminRecomBooksRepository.save(recom);
        });
    }
}
