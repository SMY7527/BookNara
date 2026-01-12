package com.booknara.booknaraPrj.admin.recomBooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookSearchResponseDto {
    private String isbn13;
    private String title;
    private String author;
}
