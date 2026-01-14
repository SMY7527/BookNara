package com.booknara.booknaraPrj.bookAPI.service.sync.naver.model;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;

/**
 * [NaverCallResult]
 * 네이버 API 호출 결과를 상태값(Status)과 데이터(Response)로 구조화한 결과 객체입니다.
 * 서비스 레이어에서 API 호출 이후의 흐름을 결정하는 판단 근거가 됩니다.
 */
public class NaverCallResult {
    private final ResponseStatus status;   // 시스템 내부에서 정의한 응답 상태
    private final NaverResponse response; // 네이버 API로부터 받은 실제 데이터 본문

    /** private 생성자를 통해 정적 팩토리 메소드 사용을 강제하여 가독성을 높임 */
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

    // --- 정적 팩토리 메소드: 호출 결과의 의미를 명확히 함 ---

    /** [성공] 유효한 도서 데이터를 확보했을 때 사용 */
    public static NaverCallResult withData(NaverResponse response) {
        return new NaverCallResult(ResponseStatus.SUCCESS_WITH_DATA, response);
    }

    /** [성공/결과없음] API 호출은 성공했으나 해당 ISBN에 매칭되는 도서가 없을 때 사용 */
    public static NaverCallResult noData() {
        return new NaverCallResult(ResponseStatus.SUCCESS_NO_DATA, null);
    }

    /** [실패/재시도] 타임아웃, 네트워크 일시 오류 등 추후 재시도가 필요한 상황에 사용 */
    public static NaverCallResult retryableFail() {
        return new NaverCallResult(ResponseStatus.RETRYABLE_FAIL, null);
    }

    /** [실패/중단] 파라미터 오류, 권한 부족 등 재시도해도 결과가 변하지 않을 상황에 사용 */
    public static NaverCallResult nonRetryFail() {
        return new NaverCallResult(ResponseStatus.NONRETRY_FAIL, null);
    }
}