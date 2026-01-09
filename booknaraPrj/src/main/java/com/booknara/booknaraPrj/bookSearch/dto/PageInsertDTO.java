package com.booknara.booknaraPrj.bookSearch.dto;

import lombok.Data;

@Data
public class PageInsertDTO {
    private int page;
    private int size;
    private int offset;
}
