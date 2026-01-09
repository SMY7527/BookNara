package com.booknara.booknaraPrj.admin.bookManagement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "BOOKS")
public class AdminBooks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Long bookId;

    // Books.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISBN13", referencedColumnName = "ISBN13", insertable = false, updatable = false)
    private AdminBookIsbn bookIsbn;

    // 만약 조인용 필드 외에 실제 값을 들고 있는 필드가 필요하다면 아래를 추가
    @Column(name = "ISBN13")
    private String isbn13;

    @Column(name = "FORMAT", nullable = false, length = 1)
    private String format = "P"; // 기본값 'P' (Physical)

    @Column(name = "BOOK_STATE", nullable = false, length = 1)
    private String bookState = "N"; // 기본값 'N' (Normal)

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}