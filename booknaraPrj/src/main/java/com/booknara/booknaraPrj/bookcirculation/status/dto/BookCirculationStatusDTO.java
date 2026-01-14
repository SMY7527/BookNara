package com.booknara.booknaraPrj.bookcirculation.status.dto;

import lombok.Data;

/**
 * [BookCirculationStatusDTO]
 * 도서 상세 화면에서 대출/예약/연장 버튼의 상태를 제어하기 위한 통합 상태 객체입니다.
 * 도서의 물리적 재고 현황과 사용자의 개인화 정보를 한 번에 담아 반환합니다.
 */
@Data
public class BookCirculationStatusDTO {
    /** 조회 대상 도서의 고유 식별자 */
    private String isbn13;

    // --- [1] 도서 재고 및 예약 가용성 (Inventory Status) ---
    /** 도서관이 보유한 정상 상태의 총 실물 권수 */
    private int ownedCnt;
    /** 현재 대출 중인 권수 */
    private int lendingCnt;
    /** 즉시 대여 가능한 권수 (ownedCnt - lendingCnt) */
    private int availableCnt;
    /** 현재 이 도서를 기다리는 예약자 수 */
    private int rsvActiveCnt;
    /** 예약 가능 여부 플래그 (현재 예약자가 10명 이상이면 'Y' -> 추가 예약 불가) */
    private String rsvLimitYn;

    // --- [2] 현재 로그인 사용자의 대출 관계 (Personal Lending) ---
    /** 내가 현재 이 도서를 빌린 상태인지 여부 ('Y'/'N') */
    private String myLendYn;
    /** 내가 이 도서를 연장할 수 있는 조건인지 여부 ('Y'/'N')
     * (조건: 연장 횟수 0회 + 반납 7일 전 등)
     */
    private String myExtendYn;
    /** 연장 요청 시 식별자로 사용할 현재 대출 ID */
    private String myLendId;

    // --- [3] 현재 로그인 사용자의 예약 관계 (Personal Reservation) ---
    /** 내가 현재 이 도서를 예약 중인지 여부 ('Y'/'N') */
    private String myRsvYn;
    /** 예약 취소 요청 시 식별자로 사용할 예약 ID */
    private String myRsvId;

    // --- [4] 사용자 서비스 이용 자격 (Account Status) ---
    /** 사용자의 계정이 관리자에 의해 차단되었거나 탈퇴 상태인지 여부 */
    private String userBlockedYn;
    /** 사용자가 현재 다른 도서를 연체 중이어서 서비스 이용이 제한되는지 여부 */
    private String userOverdueYn;
}