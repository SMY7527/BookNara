package com.booknara.booknaraPrj.bookAPI.service.batch.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * [BatchScheduleUtil]
 * 배치 처리 및 API 쿨다운 시간 계산을 위한 유틸리티 클래스
 */
public final class BatchScheduleUtil {

    private BatchScheduleUtil() {
        // 인스턴스화 방지
    }

    /**
     * 한국 표준시(KST) 기준, "내일 00:00:00"에서 특정 시간(분)을 더한 시각을 밀리초로 반환
     * 주로 일일 API 호출 한도가 초기화되는 시점으로 쿨다운을 설정할 때 사용함
     *
     * @param delayMinutes 자정 이후 추가로 대기할 시간 (분 단위)
     * @return 계산된 시각의 Epoch Millis
     */
    public static long nextKstMidnightPlusMinutes(long delayMinutes) {
        ZoneId kst = ZoneId.of("Asia/Seoul"); // 서버 환경과 무관하게 한국 시간 고정

        return ZonedDateTime.now(kst)
                .plusDays(1)                      // 날짜를 다음 날로 변경
                .toLocalDate()                    // 시간 정보를 버리고 날짜만 취함
                .atStartOfDay(kst)                // 다음 날 00:00:00으로 설정
                .plusMinutes(delayMinutes)        // 설정된 지연 시간(분) 추가
                .toInstant()                      // UTC 타임스탬프로 변환
                .toEpochMilli();                  // 밀리초 단위 값 추출
    }
}