package com.booknara.booknaraPrj.reviewstatus.mapper;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewStatusMapper {

    // 단건(상세페이지용)
    ReviewStatusDTO selectByIsbn(@Param("isbn13") String isbn13);

    // 여러 ISBN 한 번에(검색목록/그리드용)
    List<ReviewStatusDTO> selectByIsbns(@Param("isbns") List<String> isbns);
}
