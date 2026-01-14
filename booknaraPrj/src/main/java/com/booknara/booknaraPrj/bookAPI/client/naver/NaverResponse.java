package com.booknara.booknaraPrj.bookAPI.client.naver;

import lombok.Data;

import java.util.List;

@Data
public class NaverResponse {
    private List<NaverDTO> items;
}
