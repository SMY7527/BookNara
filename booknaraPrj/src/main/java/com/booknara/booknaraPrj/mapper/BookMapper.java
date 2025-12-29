package com.booknara.booknaraPrj.mapper;

import com.booknara.booknaraPrj.domain.BookDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    // 도서 목록을 일괄 INSERT 하기 위한 MyBatis 매퍼
    // 배치 적재(1만 건 단위)에서 사용
    int insertBook(@Param("list") List<BookDTO> list);
}
