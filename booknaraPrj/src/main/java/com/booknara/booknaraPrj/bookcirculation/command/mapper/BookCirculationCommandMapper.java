package com.booknara.booknaraPrj.bookcirculation.command.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookCirculationCommandMapper {

    // 유저 상태
    String selectUserBlockedYn(@Param("userId") String userId);
    String selectUserOverdueYn(@Param("userId") String userId);

    int countMyActiveLends(String userId);
    Integer selectMaxLendCount();


    // 대여 정책
    int existsActiveLendByUserAndIsbn(@Param("userId") String userId,
                                      @Param("isbn13") String isbn13);

    Long selectAvailableBookIdForUpdate(@Param("isbn13") String isbn13);

    int insertLend(@Param("lendId") String lendId,
                   @Param("bookId") Long bookId,
                   @Param("userId") String userId,
                   @Param("isbn13") String isbn13);

    int extendIfAllowed(@Param("lendId") String lendId,
                        @Param("userId") String userId);

    // 예약 정책
    int countActiveReservations(@Param("isbn13") String isbn13);

    int insertReservation(@Param("rsvId") String rsvId,
                          @Param("userId") String userId,
                          @Param("isbn13") String isbn13);

    int cancelReservation(@Param("rsvId") String rsvId,
                          @Param("userId") String userId);

    // 반납
    int markReturnBox(@Param("lendId") String lendId,
                      @Param("userId") String userId,
                      @Param("boxId") Long boxId);

    int confirmReturn(@Param("lendId") String lendId,
                      @Param("userId") String userId);
}
