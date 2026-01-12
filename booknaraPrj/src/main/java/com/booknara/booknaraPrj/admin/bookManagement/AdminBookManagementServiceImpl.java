package com.booknara.booknaraPrj.admin.bookManagement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookManagementServiceImpl implements AdminBookManageMentService {

    private final AdminBookManagementRepository adminBookManagementRepository;
    private final AdminBookIsbnRepository isbnRepository;
    private final AdminGenreRepository genreRepository;

    /**
     * [통합 및 수정] 검색어와 도서 상태 필터를 모두 적용한 리스트 조회
     */
    @Override
    public Page<AdminBookListResponseDto> getBookList(int page, String keyword, String bookState) {
        // 1. 페이징 설정 (한 페이지에 50개씩, 최신 등록순)
        Pageable pageable = PageRequest.of(page, 50, Sort.by("createdAt").descending());

        // 2. 검색어 정제 (null 혹은 빈 문자열 처리)
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        // 3. 상태 필터 정제 (null 혹은 빈 문자열 처리)
        String stateFilter = (bookState == null || bookState.trim().isEmpty()) ? null : bookState.trim();

        // 4. 리포지토리의 findByFilters 호출 (이전에 만든 @Query 메서드)
        // 만약 검색어와 상태가 모두 없으면 전체 조회가 되도록 쿼리가 짜여있어야 함
        Page<AdminBookListResponseDto> result = adminBookManagementRepository.findByFilters(searchKeyword, stateFilter, pageable);

        // 디버깅 로그 (필요 시)
        System.out.println("DEBUG: 필터링 결과 개수 = " + result.getTotalElements());

        return result;
    }

    @Override
    @Transactional // 수정 작업이므로 Transactional 필수
    public void updateStatus(String isbn13, String bookState) {
        adminBookManagementRepository.updateBookState(isbn13, bookState);
    }

    @Override
    @Transactional // ★ 중요: 여기에 @Transactional을 추가하여 'readOnly=true'를 덮어씌웁니다.
    public void saveBookWithGenre(AdminBookSaveRequestDto dto) {
        // 1. 장르 존재 여부 확인 및 조회
        AdminGenre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장르 ID입니다: " + dto.getGenreId()));

        // 2. AdminBookIsbn 생성 (도서의 메타 정보)
        // DTO에 있는 필드들을 엔티티의 builder에 매핑합니다.
        AdminBookIsbn bookIsbn = AdminBookIsbn.builder()
                .isbn13(dto.getIsbn13())
                .bookTitle(dto.getBookTitle())
                .authors(dto.getAuthors())
                .publisher(dto.getPublisher())
                .pubDate(dto.getPubDate())
                .naverImage(dto.getNaverImage())
                .aladinImageBig(dto.getAladinImageBig())
                .adminGenre(genre)
                .eBookYn(dto.getEBookYn() != null ? dto.getEBookYn() : "N") // 값이 없으면 "N"으로 세팅
                .dataHash("GEN-HASH-" + dto.getIsbn13())
                .build();

        isbnRepository.save(bookIsbn);

        // 3. AdminBooks 생성 (실제 관리되는 도서 인스턴스)
        // 엔티티 구조에 맞춰 생성자 혹은 Builder 사용
        AdminBooks book = AdminBooks.builder()
                .bookIsbn(bookIsbn)
                .isbn13(dto.getIsbn13())
                .bookState("N") // 초기 상태: 대여가능
                .format("P")    // 초기 포맷: 종이책
                .build();

        adminBookManagementRepository.save(book); // 변수명 일치 확인.save(book);
    }
}