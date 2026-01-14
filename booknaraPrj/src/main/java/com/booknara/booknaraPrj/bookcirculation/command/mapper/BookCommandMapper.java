package com.booknara.booknaraPrj.bookcirculation.command.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * [BookCommandMapper]
 * 도서의 대출, 예약, 반납 등 상태 변경(Command)을 수반하는 모든 DB 조작을 담당합니다.
 * 도서관의 운영 정책(Policy)이 SQL의 조건절(WHERE)로 녹아들어 있습니다.
 */
@Mapper
public interface BookCommandMapper {

    // --- [1] 사용자 검증 (User Validation) ---

    /** 사용자 정지 또는 탈퇴 여부 조회 ('Y'/'N') */
    String selectUserBlockedYn(@Param("userId") String userId);

    /** 사용자의 현재 연체 도서 보유 여부 조회 ('Y'/'N') */
    String selectUserOverdueYn(@Param("userId") String userId);

    /** 사용자가 현재 대출 중인 총 권수 조회 */
    int countMyActiveLends(String userId);

    /** 시스템 설정에 정의된 1인당 최대 대출 가능 권수 조회 */
    Integer selectMaxLendCount();


    // --- [2] 대여 정책 (Lending Policy) ---

    /** 해당 유저가 동일한 도서(ISBN)를 이미 대출 중인지 확인 */
    int existsActiveLendByUserAndIsbn(@Param("userId") String userId,
                                      @Param("isbn13") String isbn13);

    /** * [핵심] 대출 가능한 실물 도서 1권의 ID를 조회하며 행 잠금(Lock)을 겁니다.
     * 동시성 이슈를 방지하기 위해 SELECT ... FOR UPDATE 구문이 포함되어야 합니다.
     */
    Long selectAvailableBookIdForUpdate(@Param("isbn13") String isbn13);

    /** 대출 기록(LENDS 테이블)을 생성합니다. */
    int insertLend(@Param("lendId") String lendId,
                   @Param("bookId") Long bookId,
                   @Param("userId") String userId,
                   @Param("isbn13") String isbn13);

    /** * 정책(연장 횟수 0회, 반납 7일 전 등)에 부합할 경우에만 반납 예정일을 업데이트합니다.
     * SQL의 WHERE 절에서 모든 조건을 검증하도록 설계되었습니다.
     */
    int extendIfAllowed(@Param("lendId") String lendId,
                        @Param("userId") String userId);


    // --- [3] 예약 정책 (Reservation Policy) ---

    /** 특정 도서(ISBN)에 걸려 있는 현재 예약 총 건수 조회 */
    int countActiveReservations(@Param("isbn13") String isbn13);

    /** 신규 예약 기록을 생성합니다. */
    int insertReservation(@Param("rsvId") String rsvId,
                          @Param("userId") String userId,
                          @Param("isbn13") String isbn13);

    /** 사용자가 직접 예약을 취소할 때 상태를 변경하거나 삭제합니다. */
    int cancelReservation(@Param("rsvId") String rsvId,
                          @Param("userId") String userId);


    // --- [4] 반납 공정 (Return Process) ---

    /** 도서가 무인 반납함에 투입되었음을 1차 기록합니다. */
    int markReturnBox(@Param("lendId") String lendId,
                      @Param("userId") String userId,
                      @Param("boxId") Long boxId);

    /** 사서가 실물을 확인하여 대출 상태를 최종 '종료' 처리하고 도서를 가용 상태로 돌립니다. */
    int confirmReturn(@Param("lendId") String lendId,
                      @Param("userId") String userId);
}