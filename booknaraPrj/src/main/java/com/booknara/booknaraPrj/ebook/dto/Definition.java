package com.booknara.booknaraPrj.ebook.dto;

import lombok.Data;

import java.util.List;

@Data
public class Definition {
    private String definition;
    private List<String> synonyms;
    private List<String> antonyms;
}
