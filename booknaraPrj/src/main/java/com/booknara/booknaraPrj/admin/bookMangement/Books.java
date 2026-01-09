package com.booknara.booknaraPrj.admin.bookMangement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "BOOKS")
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Long bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISBN13")
    private BookIsbn bookIsbn;

    @Column(name = "BOOK_STATE", nullable = false)
    private String bookState;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}