package com.booknara.booknaraPrj.bookAPI.domain;

/**
 * [ResponseStatus]
 * 외부 API(알라딘, 네이버 등) 호출 결과에 따른 데이터 상태 정의
 * BOOK_ISBN_TEMP 테이블의 NAVER_RES_STATUS, ALADIN_RES_STATUS 컬럼과 매핑됨
 */
public enum ResponseStatus {

    /** 0: 초기 상태. 아직 해당 API로 요청을 보내지 않음 */
    NOT_TRIED(0, "미시도"),

    /** 1: 호출 성공 및 유효한 도서 데이터 확보 완료 */
    SUCCESS_WITH_DATA(1, "성공(데이터 있음)"),

    /** 2: 호출은 성공했으나 해당 API 측에 데이터가 존재하지 않음 (더 이상 시도 불필요) */
    SUCCESS_NO_DATA(2, "성공(데이터 없음)"),

    /** 3: 타임아웃, 429(Rate Limit) 등 일시적 오류. 나중에 다시 시도 가능 */
    RETRYABLE_FAIL(3, "재시도 가능 실패"),

    /** 4: 잘못된 파라미터, 키 권한 박탈 등 로직 수정 없이는 해결 안 되는 오류 */
    NONRETRY_FAIL(4, "재시도 불가 실패");

    private final int code;
    private final String description;

    ResponseStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    /** DB에 저장된 정수형 코드를 Enum 객체로 역변환 */
    public static ResponseStatus fromCode(int code) {
        for (ResponseStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 ResponseStatus 코드입니다: " + code);
    }
}