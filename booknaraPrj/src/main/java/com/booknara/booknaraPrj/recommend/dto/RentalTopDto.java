package com.booknara.booknaraPrj.recommend.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalTopDto {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;
    private String bookImg;
    private int rentCnt;
}

