package com.booknara.booknaraPrj.recommend.mapper;

import com.booknara.booknaraPrj.recommend.dto.RentalTopDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecommendMapper {
    List<Map<String, Object>> selectAgeGenderTopBooks();

    List<RentalTopDto> selectTopRentalBooks();

    List<RentalTopDto> selectMonthlyBestSeller();

}

