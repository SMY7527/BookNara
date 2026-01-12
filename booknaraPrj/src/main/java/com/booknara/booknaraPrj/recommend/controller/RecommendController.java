package com.booknara.booknaraPrj.recommend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecommendController {

    /**
     * 맞춤 추천 메인 페이지
     */
    @GetMapping("/recommend")
    public String recommendPage() {
        return "recommend";   // templates/recommend.html
    }


}
