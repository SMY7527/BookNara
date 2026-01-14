package com.booknara.booknaraPrj.bookAPI.domain;

/**
 * [TempStatus]
 * BOOK_ISBN_TEMP(Staging) 테이블의 데이터 처리 상태(STATUS_CD) 정의 클래스
 * 데이터 수집(Collect) -> 검증(Verify) -> 이관(Merge) 프로세스를 제어함
 */
public final class TempStatus {

    /** 인스턴스화 방지 (상수 전용 클래스) */
    private TempStatus() {}

    /** * 0: 미준수 상태 (NOTREADY)
     * 외부 API(정보나루, 네이버, 알라딘) 중 필수 정보가 모두 수집되지 않은 초기 상태
     */
    public static final byte NOTREADY = 0;

    /** * 1: 이관 준비 완료 (READY)
     * 모든 외부 API 수집이 완료되어 마스터 테이블(BOOK_ISBN)로 옮길 수 있는 상태
     */
    public static final byte READY    = 1;

    /** * 2: 이관 완료 (MERGED)
     * 마스터 테이블로 데이터가 최종 반영되어 처리가 종료된 상태 (아카이빙 용도)
     */
    public static final byte MERGED   = 2;
}