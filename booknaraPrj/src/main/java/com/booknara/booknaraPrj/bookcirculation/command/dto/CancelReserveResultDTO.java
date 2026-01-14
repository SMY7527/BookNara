package com.booknara.booknaraPrj.bookcirculation.command.dto;

import lombok.Data;

@Data
public class CancelReserveResultDTO {
    private String rsvId;
    private String cancelledYn; // 'Y'/'N'
}