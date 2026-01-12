package com.booknara.booknaraPrj.recommend.controller;

import com.booknara.booknaraPrj.recommend.dto.RentalTopDto;
import com.booknara.booknaraPrj.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendApiController {

    private final RecommendService recommendService;

    //연령,성별
    @GetMapping("/age-gender")
    public Map<String, Object> ageGender() {

        List<Map<String, Object>> rows =
                recommendService.getAgeGenderRecommend();

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            int age = (int) row.get("AGE_GROUP");
            String gender = row.get("GENDER").equals("M") ? "남성" : "여성";
            String key = age + "대 " + gender;

            Map<String, Object> book = new HashMap<>();
            book.put("title", row.get("BOOK_TITLE"));
            book.put("isbn", row.get("ISBN13"));

            grouped
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(book);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((k, v) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("age", k);
            m.put("books", v);
            result.add(m);
        });

        return Map.of("data", result);
    }


    //대여순
    @GetMapping("/rental")
    public List<RentalTopDto> rentalTop6() {
        return recommendService.getTopRentalBooks();
    }

    //월간베스트셀러
    @GetMapping("/bestseller")
    public List<RentalTopDto> bestseller() {
        return recommendService.getMonthlyBestSeller();
    }
}
