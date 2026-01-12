package com.booknara.booknaraPrj.recommend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingBookDTO {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String naverImage;
    private double ratingAvg;
}
