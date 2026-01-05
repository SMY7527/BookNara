package com.booknara.booknaraPrj.ebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class SaveCfiRequest {
    @NotBlank
    private String cfi;
    @NotBlank
    private String pct;
    @NotBlank
    private String href;
}
