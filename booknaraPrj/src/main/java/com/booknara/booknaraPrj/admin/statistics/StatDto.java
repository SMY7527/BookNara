package com.booknara.booknaraPrj.admin.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatDto {
    private String label;
    private Long count;
    private Double percentage;
}