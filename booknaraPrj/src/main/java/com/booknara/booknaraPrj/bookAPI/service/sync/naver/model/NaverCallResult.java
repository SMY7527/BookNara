package com.booknara.booknaraPrj.bookAPI.service.sync.naver.model;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;

public class NaverCallResult {
    private final ResponseStatus status;
    private final NaverResponse response;

    private NaverCallResult(ResponseStatus status, NaverResponse response) {
        this.status = status;
        this.response = response;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public NaverResponse getResponse() {
        return response;
    }

    public static NaverCallResult withData(NaverResponse response) {
        return new NaverCallResult(ResponseStatus.SUCCESS_WITH_DATA, response);
    }

    public static NaverCallResult noData() {
        return new NaverCallResult(ResponseStatus.SUCCESS_NO_DATA, null);
    }

    public static NaverCallResult retryableFail() {
        return new NaverCallResult(ResponseStatus.RETRYABLE_FAIL, null);
    }

    public static NaverCallResult nonRetryFail() {
        return new NaverCallResult(ResponseStatus.NONRETRY_FAIL, null);
    }
}
