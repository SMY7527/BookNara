package com.booknara.booknaraPrj.bookAPI.service.sync.naver.policy;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverClient;
import com.booknara.booknaraPrj.bookAPI.service.batch.util.BatchScheduleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverRateLimitManager {

    private final NaverClient naverClient;

    // keyIndex별 429 누적 횟수
    private final ConcurrentHashMap<Integer, Integer> daily429Count = new ConcurrentHashMap<>();

    // 정책값들(기존과 동일)
    private static final int DAILY_429_COUNT_LIMIT = 3;
    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // 15초
    private static final long DELAY_MINUTES = 5;

    /**
     * 429 발생 시 처리
     * - limit 미만: 짧은 쿨다운
     * - limit 이상: 다음날 00:05까지 쿨다운(사실상 일일 소진 취급)
     */
    public void on429(Integer keyIdx, String isbn13) {
        if (keyIdx == null) return;

        int count = daily429Count.merge(keyIdx, 1, Integer::sum);

        if (count >= DAILY_429_COUNT_LIMIT) {
            long until = BatchScheduleUtil.nextKstMidnightPlusMinutes(DELAY_MINUTES);
            naverClient.cooldownKeyUntil(keyIdx, until);
            daily429Count.remove(keyIdx);

            log.warn("naver key exhausted(429) keyIdx={} until={} isbn13={}", keyIdx, until, isbn13);
            return;
        }

        naverClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
    }

    /**
     * 성공 시 누적 429 카운트 리셋(선택: 기존 알라딘과 맞추고 싶으면 사용)
     */
    public void resetOnSuccess(Integer keyIdx) {
        if (keyIdx == null) return;
        daily429Count.remove(keyIdx);
    }
}

