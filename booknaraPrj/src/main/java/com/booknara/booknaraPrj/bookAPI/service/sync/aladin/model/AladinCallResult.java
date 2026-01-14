package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;

/**
 * [AladinCallResult]
 * 알라딘 API 호출 결과를 분석하여 서비스 레이어에 전달하는 결과 래퍼 클래스
 * 호출 성공 여부뿐만 아니라 배치 반복 루프의 지속 여부(stopLoop)를 결정함
 */
public class AladinCallResult {
    private final ResponseStatus status;   // 시스템 내부 관리용 응답 상태 (SUCCESS, FAIL 등)
    private final AladinResponse response; // 알라딘에서 수신한 실제 데이터 본문
    private final String errorCode;        // 알라딘 측에서 보낸 에러 코드 (분석용)
    private final boolean stopLoop;        // API 한도 초과 등으로 인해 전체 루프를 중단해야 하는지 여부

    // 생성자를 private으로 제한하고 정적 팩토리 메소드 사용을 강제하여 객체 생성 의도를 명확히 함
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

    /** [상태: 중단] 일일 할당량 초과 등 더 이상 호출이 불가능할 때 사용 (작업 즉시 정지) */
    public static AladinCallResult stopForToday(String errorCode) {
        return new AladinCallResult(ResponseStatus.RETRYABLE_FAIL, null, errorCode, true);
    }

    /** [상태: 성공] 유효한 도서 데이터를 성공적으로 수신했을 때 사용 */
    public static AladinCallResult withData(AladinResponse response) {
        return new AladinCallResult(ResponseStatus.SUCCESS_WITH_DATA, response, null, false);
    }

    /** [상태: 데이터 없음] 호출은 성공했으나 검색 결과(ISBN)가 존재하지 않을 때 사용 */
    public static AladinCallResult noData() {
        return new AladinCallResult(ResponseStatus.SUCCESS_NO_DATA, null, null, false);
    }

    /** [상태: 재시도 가능 실패] 타임아웃 등 일시적 오류 발생 시 사용 (다음 루프에서 재시도) */
    public static AladinCallResult retryableFail(String errorCode) {
        return new AladinCallResult(ResponseStatus.RETRYABLE_FAIL, null, errorCode, false);
    }

    /** [상태: 재시도 불가 실패] 잘못된 파라미터 등 다시 시도해도 실패할 것이 명확할 때 사용 */
    public static AladinCallResult nonRetryFail(String errorCode) {
        return new AladinCallResult(ResponseStatus.NONRETRY_FAIL, null, errorCode, false);
    }
}