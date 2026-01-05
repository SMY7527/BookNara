package com.booknara.booknaraPrj.admin.bookMangement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "GENRE")
public class Genre {
    @Id
    @Column(name = "GENRE_ID")
    private Integer genreId;

    @Column(name = "GENRE_NM", nullable = false)
    private String genreNm;

    @Column(name = "MALL", nullable = false)
    private String mall;
}