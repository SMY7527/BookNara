package com.booknara.booknaraPrj.admin.bookManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
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

    // 조인 컬럼 설정 (이것이 유일한 isbn13 통로s여야 함)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISBN13", referencedColumnName = "ISBN13")
    @JsonIgnore // [이것이 핵심입니다] JSON/Thymeleaf가 부모를 다시 읽지 않게 합니다.
    private AdminBookIsbn bookIsbn;

    @Column(name = "BOOK_STATE", nullable = false, length = 1)
    @Builder.Default  // 빌더 사용 시 기본값 유지
    private String bookState = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // DB DDL에 FORMAT 컬럼이 없다면 반드시 삭제하거나 아래처럼 처리하세요.
    @Transient
    private String format = "P";
}