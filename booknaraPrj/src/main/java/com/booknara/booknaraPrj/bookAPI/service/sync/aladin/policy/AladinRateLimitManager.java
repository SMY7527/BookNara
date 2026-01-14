package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.policy;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [AladinRateLimitManager]
 * 알라딘 API 키별 호출 제한 상태를 모니터링하고 쿨다운(사용 유예)을 제어하는 관리 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AladinRateLimitManager {

    private final AladinClient aladinClient;

    // --- 쿨다운 설정값 ---
    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // HTTP 429 발생 시 15초간 유예
    private static final long KEY_COOLDOWN_ON_5XX_MS = 5_000;  // HTTP 5xx 발생 시 5초간 유예

    // --- 429(Rate Limit) 정책 ---
    private static final int DAILY_429_COUNT_LIMIT = 3; // 429 에러가 3회 누적되면 해당 키는 당일 사용 종료
    private final ConcurrentHashMap<Integer, Integer> daily429Count = new ConcurrentHashMap<>();

    // --- 시간대 설정 ---
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5; // 자정 초기화 후 안정화를 위해 5분 추가 여유 대기

    /**
     * 알라딘 전용 에러코드 10(일일 할당량 초과) 발생 시 처리
     * 해당 API 키를 다음날 자정(+5분)까지 즉시 사용 중지시킵니다.
     */
    public void onDailyLimit10(Integer keyIdx, String isbn13, String tag) {
        if (keyIdx == null) return;

        long until = getNextScheduledTime();
        aladinClient.cooldownKeyUntil(keyIdx, until);

        log.warn("aladin daily limit exceeded({}). keyIdx={} until={} isbn13={}",
                tag, keyIdx, Instant.ofEpochMilli(until).atZone(KST_ZONE), isbn13);
    }

    /**
     * HTTP 429(Too Many Requests) 발생 시 처리
     * 1. 3회 미만 발생 시: 짧은 시간(15초) 동안 키 사용 유예
     * 2. 3회 이상 누적 시: 해당 키의 당일 할당량이 소진된 것으로 간주하여 다음날까지 사용 중지
     */
    public void on429(Integer keyIdx, String isbn13) {
        if (keyIdx == null) return;

        int count = daily429Count.merge(keyIdx, 1, Integer::sum);

        if (count >= DAILY_429_COUNT_LIMIT) {
            long until = getNextScheduledTime();
            aladinClient.cooldownKeyUntil(keyIdx, until);
            daily429Count.remove(keyIdx); // 누적 카운트 초기화

            log.warn("aladin key exhausted(429). keyIdx={} until={} isbn13={}",
                    keyIdx, Instant.ofEpochMilli(until).atZone(KST_ZONE), isbn13);
            return;
        }

        // 일시적인 과부하인 경우 짧은 쿨다운 적용
        aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
    }

    /** HTTP 5xx(서버 오류) 발생 시 처리: 5초간 짧은 쿨다운 후 재시도 허용 */
    public void on5xx(Integer keyIdx) {
        if (keyIdx == null) return;
        aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_5XX_MS);
    }

    /** API 호출 성공 시 해당 키의 429 누적 에러 카운트를 리셋하여 유연한 운영 보장 */
    public void reset429OnSuccess(Integer keyIdx) {
        if (keyIdx == null) return;
        daily429Count.remove(keyIdx);
    }

    /** 한국 시간(KST) 기준 익일 자정 5분 뒤의 타임스탬프 계산 */
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