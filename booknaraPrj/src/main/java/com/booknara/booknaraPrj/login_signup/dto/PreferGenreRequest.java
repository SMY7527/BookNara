package com.booknara.booknaraPrj.login_signup.dto;

import lombok.Data;

import java.util.List;

@Data
public class PreferGenreRequest {
    private List<Integer> genreIds;
}