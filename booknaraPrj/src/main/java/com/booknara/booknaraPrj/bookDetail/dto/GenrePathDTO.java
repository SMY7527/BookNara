package com.booknara.booknaraPrj.bookDetail.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenrePathDTO {

    //GENRE.MALL (국내도서/외국도서/전자책/음반/DVD)
    private String mall;
    //Breadcrumb 목록 (예: 전자책 > 소설 > 영미소설 > 영미소설 일반)

    private List<GenreCrumbDTO> crumbs = new ArrayList<>();
}
