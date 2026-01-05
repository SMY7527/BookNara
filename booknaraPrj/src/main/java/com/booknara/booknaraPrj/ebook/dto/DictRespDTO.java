package com.booknara.booknaraPrj.ebook.dto;

import lombok.Data;

import java.util.List;

@Data
public class DictRespDTO {
    private String word;
    private String phonetic;
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;
    private List<Meaning> meanings_kor;
    private License license;
    private List<String> sourceUrls;
}
