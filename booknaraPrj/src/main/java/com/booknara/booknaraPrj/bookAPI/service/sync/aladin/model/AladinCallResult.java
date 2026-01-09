package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;

public class AladinCallResult {
    private final ResponseStatus status;
    private final AladinResponse response;
    private final String errorCode;
    private final boolean stopLoop;

    private AladinCallResult(ResponseStatus status, AladinResponse response, String errorCode, boolean stopLoop) {
        this.status = status;
        this.response = response;
        this.errorCode = errorCode;
        this.stopLoop = stopLoop;
    }

    public ResponseStatus getStatus() { return status; }
    public AladinResponse getResponse() { return response; }
    public String getErrorCode() { return errorCode; }
    public boolean isStopLoop() { return stopLoop; }

    public static AladinCallResult stopForToday(String errorCode) {
        return new AladinCallResult(ResponseStatus.RETRYABLE_FAIL, null, errorCode, true);
    }

    public static AladinCallResult withData(AladinResponse response) {
        return new AladinCallResult(ResponseStatus.SUCCESS_WITH_DATA, response, null, false);
    }

    public static AladinCallResult noData() {
        return new AladinCallResult(ResponseStatus.SUCCESS_NO_DATA, null, null, false);
    }

    public static AladinCallResult retryableFail(String errorCode) {
        return new AladinCallResult(ResponseStatus.RETRYABLE_FAIL, null, errorCode, false);
    }

    public static AladinCallResult nonRetryFail(String errorCode) {
        return new AladinCallResult(ResponseStatus.NONRETRY_FAIL, null, errorCode, false);
    }
}
