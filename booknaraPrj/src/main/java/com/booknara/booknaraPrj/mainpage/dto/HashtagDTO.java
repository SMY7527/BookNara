package com.booknara.booknaraPrj.mainpage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor          // JSON 역직렬화 대비
@AllArgsConstructor
public class HashtagDTO {

    private int genreId;
    private String label;
}