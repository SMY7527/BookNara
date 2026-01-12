package com.booknara.booknaraPrj.mypage.event;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventMapper {

    List<EventHistoryDto> selectMyEventHistory(
            @Param("userId") String userId,
            @Param("type") String type
    );
}



