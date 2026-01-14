package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

@Data
public class ExtendResultDTO {
    private String lendId;
    private String extendedYn; // 'Y'/'N'
    private String message;
}
