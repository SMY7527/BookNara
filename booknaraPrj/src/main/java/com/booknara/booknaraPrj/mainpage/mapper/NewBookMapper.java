package com.booknara.booknaraPrj.mainpage.mapper;

import com.booknara.booknaraPrj.mainpage.dto.NewBookDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NewBookMapper {
    List<NewBookDTO> selectLatestBooks();
}

