package com.booknara.booknaraPrj.recommend.service;

import com.booknara.booknaraPrj.recommend.dto.RentalTopDto;
import com.booknara.booknaraPrj.recommend.mapper.RecommendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RecommendMapper recommendMapper;

    //연령,성별
    public List<Map<String, Object>> getAgeGenderRecommend() {
        return recommendMapper.selectAgeGenderTopBooks();
    }
    //대여순
    public List<RentalTopDto> getTopRentalBooks() {
        return recommendMapper.selectTopRentalBooks();
    }

    //월간베스트셀러
    public List<RentalTopDto> getMonthlyBestSeller() {
        return recommendMapper.selectMonthlyBestSeller();
    }
}
