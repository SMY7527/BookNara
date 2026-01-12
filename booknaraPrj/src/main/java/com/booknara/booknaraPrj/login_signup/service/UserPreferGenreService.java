package com.booknara.booknaraPrj.login_signup.service;

import com.booknara.booknaraPrj.login_signup.mapper.UserPreferGenreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferGenreService {

    private final UserPreferGenreMapper mapper;

    public void save(String userId, List<Integer> genreIds) {
        for (Integer genreId : genreIds) {
            mapper.insert(userId, genreId);
        }
    }
}