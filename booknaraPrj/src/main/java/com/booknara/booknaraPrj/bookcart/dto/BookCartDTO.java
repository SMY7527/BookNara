package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

@Data
public class BookCartDTO {
    private Long cartId;
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String naverImage;
    private String aladinImageBig;
    private String ebookYn;
    private Boolean lendableYn;
}

