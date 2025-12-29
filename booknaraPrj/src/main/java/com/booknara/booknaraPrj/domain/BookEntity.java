package com.booknara.booknaraPrj.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_isbn", indexes = {
        @Index(name = "idx_book_isbn_bookname", columnList = "bookname")
})
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BookEntity {
    @Id
    @Column(name = "isbn13", length = 20) //
    private String isbn13;

    @Column(nullable = false, length = 300)
    private String bookname;

    @Column(length = 500)
    private String authors;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String publisher;

    private java.sql.Date pubdate;

    @Column(length = 2048)
    private String image;

    @Column(length = 2048)
    private String epub;

    @Column(name = "category_no")
    private Integer categoryNo;

    @Column(name = "class_no")
    private Integer classNo;

    @Column(name = "data_hash", length = 64)
    private String dataHash;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public BookEntity(String isbn13, String bookname, String authors, String description,
                      String publisher, java.sql.Date pubdate, String image, String epub,
                      Integer categoryNo, Integer classNo, String dataHash) {
        this.isbn13 = isbn13;
        this.bookname = bookname;
        this.authors = authors;
        this.description = description;
        this.publisher = publisher;
        this.pubdate = pubdate;
        this.image = image;
        this.epub = epub;
        this.categoryNo = categoryNo;
        this.classNo = classNo;
        this.dataHash = dataHash;
    }

}
