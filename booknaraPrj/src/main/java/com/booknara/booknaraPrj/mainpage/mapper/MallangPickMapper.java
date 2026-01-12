package com.booknara.booknaraPrj.mainpage.mapper;


import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallangPickMapper {

    List<MallangPickDTO> findRandomBooksByGenre(
            @Param("genreId") int genreId,
            @Param("limit") int limit
    );
}
