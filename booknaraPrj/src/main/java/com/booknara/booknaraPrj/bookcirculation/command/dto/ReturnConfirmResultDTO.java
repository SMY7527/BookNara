package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

@Data
public class ReturnConfirmResultDTO {
    private String lendId;
    private String returnedYn; // 'Y'/'N'
}