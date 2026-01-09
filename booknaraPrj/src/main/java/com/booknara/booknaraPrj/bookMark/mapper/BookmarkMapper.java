package com.booknara.booknaraPrj.bookMark.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookmarkMapper {

    /** 북마크 여부 확인 (Y/N) */
    String selectBookmarkYn(@Param("isbn13") String isbn13,
                            @Param("userId") String userId);

    /** 북마크 등록 (없으면 INSERT, 있으면 Y로 UPDATE) */
    int upsertBookmark(@Param("isbn13") String isbn13,
                       @Param("userId") String userId);

    /** 북마크 해제 (Y → N) */
    int cancelBookmark(@Param("isbn13") String isbn13,
                       @Param("userId") String userId);

    /** (선택) 특정 책 북마크 개수 */
    int countBookmarkByIsbn(@Param("isbn13") String isbn13);
}
