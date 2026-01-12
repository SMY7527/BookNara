package com.booknara.booknaraPrj.recommend.service;


import com.booknara.booknaraPrj.recommend.dto.RatingBookDTO;
import com.booknara.booknaraPrj.recommend.mapper.RecommendRatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendRatingService {

    private final RecommendRatingMapper mapper;

    public List<RatingBookDTO> getTopRatedBooks() {
        return mapper.findTopRatedBooks();
    }
}

