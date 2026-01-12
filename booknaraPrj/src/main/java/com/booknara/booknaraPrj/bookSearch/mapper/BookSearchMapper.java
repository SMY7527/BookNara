package com.booknara.booknaraPrj.bookSearch.mapper;

import com.booknara.booknaraPrj.bookSearch.dto.BookSearchConditionDTO;
import com.booknara.booknaraPrj.bookSearch.dto.BookSearchDTO;
import com.booknara.booknaraPrj.bookSearch.dto.PageInsertDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookSearchMapper {

    List<BookSearchDTO> searchBooks(
            @Param("cond") BookSearchConditionDTO cond,
            @Param("page") PageInsertDTO page,
            @Param("userId") String userId
    );

    long countBooks(
            @Param("cond") BookSearchConditionDTO cond,
            @Param("userId") String userId
    );
}

