package com.booknara.booknaraPrj.recommend.mapper;

import com.booknara.booknaraPrj.recommend.dto.RatingBookDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecommendRatingMapper {
    List<RatingBookDTO> findTopRatedBooks();
}
