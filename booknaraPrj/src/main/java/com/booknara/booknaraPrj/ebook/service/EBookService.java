package com.booknara.booknaraPrj.ebook.service;

import com.booknara.booknaraPrj.ebook.dto.MyEBookItemDTO;
import com.booknara.booknaraPrj.ebook.dto.SaveCfiRequest;
import com.booknara.booknaraPrj.ebook.repository.EBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EBookService {
    private final EBookRepository repo;

    @Value("${ebook.storage.path}")
    private String ebookBasePath;

    // 도서 epub 검색
    public String findEpub(String isbn) {
        return repo.getEpubByISBN13(isbn);
    }

    public String findEpub(long id) {
        return repo.getEpubByBookID(id);
    }

    public String findIsbn(long id) {
        return repo.getIsbnByBookID(id);
    }

    public SaveCfiRequest findCfi(String user, String isbn) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", user);
        map.put("isbn", isbn);
        return repo.getCfi(map);
    }

    // 도서가 회원이 대여중인 도서가 맞는지 검증
    public boolean canReadBook(String userId, Long bookId) {
        return repo.existsActiveLendByBook(userId, bookId);
    }
    public boolean canReadBook(String userId, String isbn) {
        return repo.existsActiveLendByIsbn(userId, isbn);
    }

    // 회원이 대여중인 전자책 리스트 검색
    public List<MyEBookItemDTO> findEBookList(String user) {
        return repo.getEBookList(user);
    }

    // epub파일 가져오기
    public ResponseEntity<Resource> getEpubResource(String epub) throws IOException {
        Path epubPath = Paths.get(ebookBasePath).resolve(epub);

        if (!Files.exists(epubPath)) {
            System.out.println("Not Found");
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(epubPath.toUri());
        System.out.println("resource : " + resource);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/epub+zip")
                .body(resource);
    }

    // cfi 저장
    public void setCfi(String userId, String isbn, String cfi, String pct, String href) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("isbn", isbn);
        map.put("cfi", cfi);
        map.put("pct", pct);
        map.put("href", href);
        repo.setCfiToHistory(map);
    }
}
