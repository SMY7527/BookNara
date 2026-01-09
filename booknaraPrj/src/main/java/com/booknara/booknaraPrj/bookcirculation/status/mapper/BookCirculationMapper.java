package com.booknara.booknaraPrj.bookcirculation.status.mapper;

import com.booknara.booknaraPrj.bookcirculation.status.dto.BookCirculationStatusDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookCirculationMapper {
    BookCirculationStatusDTO getStatus(@Param("isbn13") String isbn13,
                                       @Param("userId") String userId);
}
