package com.booknara.booknaraPrj.admin.bookMangement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "BOOK_ISBN")
public class BookIsbn {
    @Id
    @Column(name = "ISBN13")
    private String isbn13;

    @Column(name = "BOOK_TITLE", nullable = false)
    private String bookTitle;

    @Column(name = "AUTHORS", nullable = false)
    private String authors;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHER", nullable = false)
    private String publisher;

    @Column(name = "PUBDATE")
    @Temporal(TemporalType.DATE)
    private LocalDate pubDate;

    @Column(name = "ALADIN_IMAGE_BIG") // 실제 DB 컬럼명과 매핑
    private String aladinImageBig;

    @Column(name = "EBOOK_YN")
    private String eBookYn;

    @Column(name = "EPUB")
    private String epub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENRE_ID")
    private Genre genre;
}