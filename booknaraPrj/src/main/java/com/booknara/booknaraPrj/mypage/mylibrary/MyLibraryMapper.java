package com.booknara.booknaraPrj.mypage.mylibrary;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyLibraryMapper {

    // ===== 조회 =====
    List<MyLendDto> selectCurrentLends(@Param("userId") String userId);

    List<MyLendDto> selectLendHistory(@Param("userId") String userId);

    List<MyReserveDto> selectMyReservations(@Param("userId") String userId);

    List<MyBookmarkDto> selectMyBookmarks(@Param("userId") String userId);

    List<MyLendDto> selectOverdueLends(@Param("userId") String userId);
    MyLendDto selectNearestDueLend(@Param("userId") String userId);

    List<MyLendDto> selectCalendarLends(@Param("userId") String userId);

    List<MyLendDto> selectCalendarActiveLends(@Param("userId") String userId);   // 반납전(연체 포함)
    List<MyLendDto> selectCalendarReturnedLends(@Param("userId") String userId); // 반납완료




    // ===== 반납 / 연장 (🔥 이게 빠져있던 핵심) =====
    int updateReturnDone(@Param("lendId") String lendId);

    int updateExtendLend(@Param("lendId") String lendId);
}
