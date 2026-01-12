package com.booknara.booknaraPrj.mainpage.mapper;

import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MallangPickMapper {

    List<MallangPickDTO> selectMallangPickBooks(int genreId);
}
