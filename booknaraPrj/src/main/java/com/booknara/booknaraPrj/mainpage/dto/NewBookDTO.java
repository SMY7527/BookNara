package com.booknara.booknaraPrj.mainpage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBookDTO {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;
    private String bookImg;
}
