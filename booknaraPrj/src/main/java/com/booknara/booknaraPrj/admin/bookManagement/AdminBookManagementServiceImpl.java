package com.booknara.booknaraPrj.admin.bookManagement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
     * [최적화 완료] 80만 건 대용량 데이터 조회
     * 인터페이스의 리턴 타입 변경(Page -> Slice)에 맞춰 수정되었습니다.
     */

    @Override
    @Transactional(readOnly = true)
    public Slice<AdminBookListResponseDto> getBookList(String bookState, String keyword, Pageable pageable) {

        // 1. 키워드 가공 (Repository 쿼리에서 LIKE %:keyword%를 썼다면 여기서 안 해도 되지만, 안전하게 처리)
        String searchKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;

        // 2. 리포지토리 호출 (이미 DTO로 결과가 나옴)
        // 쿼리에서 'new DTO(...)'를 사용했으므로 booksSlice의 내용물은 이미 DTO입니다.
        return adminBookManagementRepository.findByFiltersDto(
                searchKeyword,
                bookState,
                pageable
        );
    }

    @Override
    @Transactional
    public void updateStatus(Long bookId, String bookState) {
        adminBookManagementRepository.updateBookState(bookId, bookState);
    }

    @Override
    @Transactional
    public void saveBookWithGenre(AdminBookSaveRequestDto dto) {
        // 1. 장르 조회
        AdminGenre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장르 ID입니다: " + dto.getGenreId()));

        // 2. AdminBookIsbn(메타 정보) 생성 및 저장
        AdminBookIsbn bookIsbn = AdminBookIsbn.builder()
                .isbn13(dto.getIsbn13())
                .bookTitle(dto.getBookTitle())
                .authors(dto.getAuthors())
                .publisher(dto.getPublisher())
                .pubDate(dto.getPubDate())
                .naverImage(dto.getNaverImage())
                .aladinImageBig(dto.getAladinImageBig())
                .adminGenre(genre)
                .eBookYn(dto.getEBookYn() != null ? dto.getEBookYn() : "N")
                .dataHash("GEN-HASH-" + dto.getIsbn13())
                .build();

        isbnRepository.save(bookIsbn);

        // 3. AdminBooks 생성 및 저장
        // [수정 포인트] 엔티티에서 String isbn13을 삭제했으므로 .isbn13() 빌더 호출을 제거합니다.
        AdminBooks book = AdminBooks.builder()
                .bookIsbn(bookIsbn) // 객체 참조를 통해 ISBN13 컬럼에 값이 들어갑니다.
                .bookState("N")
                // .format("P") // 만약 엔티티에서 format 필드를 삭제/주석처리했다면 이것도 제외하세요.
                .build();

        adminBookManagementRepository.save(book);
    }
}