package com.booknara.booknaraPrj.admin.bookManagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "GENRE")
public class AdminGenre {
    @Id
    @Column(name = "GENRE_ID")
    private Integer genreId;

    @Column(name = "GENRE_NM", nullable = false)
    private String genreNm;

    @Column(name = "MALL", nullable = false)
    private String mall;

    // 계층 구조를 위한 자기 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @JsonIgnore
    private AdminGenre parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private List<AdminGenre> children = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}