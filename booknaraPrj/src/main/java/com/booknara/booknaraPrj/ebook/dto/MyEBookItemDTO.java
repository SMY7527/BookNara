package com.booknara.booknaraPrj.ebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
@Data
public class MyEBookItemDTO {
    // 대여번호
    String lendId;
    // 도서번호
    int bookId;
    // 책 ISBN
    String ISBN13;
    // 책 제목
    String bookTitle;
    // 책 저자
    String authors;
    // 책 표지
    String coverUrl;
    // 전자책 여부
    char ebookYN;
    // cfi정보
    String cfi;
}
