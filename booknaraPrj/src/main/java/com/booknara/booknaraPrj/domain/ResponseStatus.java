package com.booknara.booknaraPrj.domain;

public enum ResponseStatus {

    NOT_TRIED(0, "미시도"),
    SUCCESS_WITH_DATA(1, "성공(데이터 있음)"),
    SUCCESS_NO_DATA(2, "성공(데이터 없음)"),
    RETRYABLE_FAIL(3, "재시도 가능 실패"),
    NONRETRY_FAIL(4, "재시도 불가 실패");

    private final int code;
    private final String description;

    ResponseStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ResponseStatus fromCode(int code) {
        for (ResponseStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ResponseStatus code=" + code);
    }
}
