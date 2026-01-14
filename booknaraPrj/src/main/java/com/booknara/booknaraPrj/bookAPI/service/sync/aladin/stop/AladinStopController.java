package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.stop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * [AladinStopController]
 * 알라딘 전체 수집 공정의 실행 여부를 제어하는 전역 스토퍼(Stopper)입니다.
 * 모든 API 키가 소진되었을 때 시스템 전체를 내일 자정까지 휴면 상태로 전환합니다.
 */
@Component
@Slf4j
public class AladinStopController {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5; // 자정 초기화 후 여유 시간

    /** * 수집 정지 해제 시점 (Epoch Millis)
     * volatile 키워드를 사용하여 멀티스레드 환경에서 최신 상태값이 즉시 반영되도록 보장합니다.
     */
    private volatile long stopUntilEpochMs = 0L;

    /** * 현재 수집 공정이 정지된 상태인지 확인합니다.
     * @return true: 정지 상태 (수집 건너뜀), false: 실행 가능 상태
     */
    public boolean isStoppedNow() {
        return stopUntilEpochMs > System.currentTimeMillis();
    }

    /** * 수집 공정을 내일 자정(+5분)까지 정지시킵니다.
     * 모든 API 키의 할당량이 소진되었음이 감지되었을 때 호출됩니다.
     */
    public void stopUntilTomorrow() {
        stopUntilEpochMs = getNextScheduledTime();
    }

    /** 현재 설정된 정지 해제 시각을 읽기 쉬운 문자열(KST 기준)로 반환합니다. */
    public String stopUntilKstString() {
        if (stopUntilEpochMs <= 0) return "N/A";
        return Instant.ofEpochMilli(stopUntilEpochMs).atZone(KST_ZONE).toString();
    }

    /** 한국 시간(KST) 기준 익일 자정 5분 뒤의 타임스탬프를 계산합니다. */
    private long getNextScheduledTime() {
        return ZonedDateTime.now(KST_ZONE)
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(KST_ZONE)
                .plusMinutes(DELAY_MINUTES)
                .toInstant()
                .toEpochMilli();
    }
}