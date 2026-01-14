package com.booknara.booknaraPrj.admin.bookManagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "BOOK_ISBN")
public class AdminBookIsbn {
    //테스트
    //owo
    //테스트2
    //ㅇㅅㅇ
    @Id
    @Column(name = "ISBN13", length = 20)
    private String isbn13;

    @Column(name = "BOOK_TITLE", nullable = false, length = 300)
    private String bookTitle;

    @Column(name = "AUTHORS", nullable = false, length = 500)
    private String authors;

    @Column(name = "DESCRIPTION", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "PUBLISHER", length = 255)
    private String publisher;

    // DB가 VARCHAR(8)이므로 String으로 유지하거나,
    // 로직에서 변환이 필요하다면 @Convert를 사용해야 합니다. 우선 생성문에 맞춰 String으로 변경합니다.
    @Column(name = "PUBDATE", nullable = false, length = 8)
    private String pubDate;

    @Column(name = "NAVER_IMAGE", length = 2048)
    private String naverImage;

    @Column(name = "ALADIN_IMAGE_BIG", nullable = false, length = 2048)
    private String aladinImageBig;

    @Column(name = "EBOOK_YN", nullable = false, length = 1)
    private String eBookYn = "N"; // 기본값 설정

    @Column(name = "EPUB", length = 2048)
    private String epub;

    // 변경 감지용 해시값 (누락된 필드 추가)
    @Column(name = "DATA_HASH", nullable = false, length = 64)
    private String dataHash;

    // 생성일 자동 관리
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일 자동 관리
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @ToString.Exclude // [추가]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENRE_ID", nullable = false)
    private AdminGenre adminGenre;

    @ToString.Exclude
    @OneToMany(mappedBy = "bookIsbn", fetch = FetchType.LAZY)
    @JsonIgnore // [이것이 핵심입니다] 부모에서 자식 리스트로 다시 내려가는 루프를 방어합니다.
    private List<AdminBooks> adminBooksList;
}