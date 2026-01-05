package com.booknara.booknaraPrj.ebook.dto;

import lombok.Data;

@Data
public class Phonetic {
    private String text;
    private String audio;
    private String sourceUrl;
    private License license;
}
