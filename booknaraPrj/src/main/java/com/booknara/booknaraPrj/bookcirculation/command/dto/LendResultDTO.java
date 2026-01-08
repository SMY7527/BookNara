package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

@Data
public class LendResultDTO {
    private String lendId;
    private Long bookId;
    private String isbn13;
}