package com.booknara.booknaraPrj.bookcart.mapper;

import com.booknara.booknaraPrj.bookcart.dto.BookCartDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookCartMapper {

    int insert(@Param("userId") String userId,
               @Param("isbn13") String isbn13);

    int delete(@Param("userId") String userId,
               @Param("cartId") Long cartId);

    int deleteAll(@Param("userId") String userId);

    int deleteByIsbn(@Param("userId") String userId, @Param("isbn13") String isbn13);

    List<BookCartDTO> selectList(@Param("userId") String userId);


}
