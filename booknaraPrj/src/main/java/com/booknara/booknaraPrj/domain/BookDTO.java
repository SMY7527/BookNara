package com.booknara.booknaraPrj.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookDTO {
    private String book_no;
    private String book_state;
    private String isbn13;
    private String bookname;
    private String authors;
    private String description;
    private String publisher;
    private String pubdate;
    private String image;
    private String epub;
    private String category_no;
    private String data_hash;
}
