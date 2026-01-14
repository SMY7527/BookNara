package com.booknara.booknaraPrj.bookSearch.mapper;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GenreMapper {
    List<GenreDTO> selectParentGenresAuto(@Param("top") int top, @Param("min") int min);
    List<GenreDTO> selectChildGenresAuto(@Param("parentId") int parentId, @Param("top") int top, @Param("min") int min);
    List<GenreDTO> selectForeignParentGenresAuto(@Param("top") int top, @Param("min") int min);
}


