package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

@Data
public class ReturnBoxResultDTO {
    private String lendId;
    private String boxedYn; // 'Y'/'N'
}