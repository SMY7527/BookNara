package com.booknara.booknaraPrj.client.naver;

import lombok.Data;

import java.util.List;

@Data
public class NaverResponse {
    private List<NaverDTO> items;
}
