package com.booknara.booknaraPrj.mainpage.mapper;


import com.booknara.booknaraPrj.mainpage.dto.LibrarianBookDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LibrarianBookMapper {

    List<LibrarianBookDTO> selectLibrarianBooks();
}

