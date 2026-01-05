package com.booknara.booknaraPrj.ebook.dto;

import lombok.Data;

import java.util.List;

@Data
public class Meaning {
    private String partOfSpeech;
    private List<Definition> definitions;
    private List<String> synonyms;
    private List<String> antonyms;
}
