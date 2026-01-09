package com.booknara.booknaraPrj.admin.recomBooks;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class AdminRecomSaveRequestDto {
    private List<String> isbns;

    public AdminRecomSaveRequestDto(List<String> isbns) {
        this.isbns = isbns;
    }
}