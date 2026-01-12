package com.booknara.booknaraPrj.recommend.controller;


import com.booknara.booknaraPrj.recommend.dto.RatingBookDTO;
import com.booknara.booknaraPrj.recommend.service.RecommendRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendRatingController {

    private final RecommendRatingService service;

    @GetMapping("/rating")
    public List<RatingBookDTO> ratingBooks() {
        return service.getTopRatedBooks();
    }
}

