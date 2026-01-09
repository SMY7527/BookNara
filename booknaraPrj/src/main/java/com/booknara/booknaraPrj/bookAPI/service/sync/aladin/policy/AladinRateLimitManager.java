package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.policy;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinRateLimitManager {

    private final AladinClient aladinClient;

    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // 15초
    private static final long KEY_COOLDOWN_ON_5XX_MS = 5_000;  // 5초

    private static final int DAILY_429_COUNT_LIMIT = 3;
    private final ConcurrentHashMap<Integer, Integer> daily429Count = new ConcurrentHashMap<>();

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5;

    public void onDailyLimit10(Integer keyIdx, String isbn13, String tag) {
        if (keyIdx == null) return;

        long until = getNextScheduledTime();
        aladinClient.cooldownKeyUntil(keyIdx, until);

        log.warn("aladin daily limit exceeded({}). keyIdx={} until={} isbn13={}",
                tag, keyIdx, Instant.ofEpochMilli(until).atZone(KST_ZONE), isbn13);
    }

    public void on429(Integer keyIdx, String isbn13) {
        if (keyIdx == null) return;

        int count = daily429Count.merge(keyIdx, 1, Integer::sum);

        if (count >= DAILY_429_COUNT_LIMIT) {
            long until = getNextScheduledTime();
            aladinClient.cooldownKeyUntil(keyIdx, until);
            daily429Count.remove(keyIdx);

            log.warn("aladin key exhausted(429). keyIdx={} until={} isbn13={}",
                    keyIdx, Instant.ofEpochMilli(until).atZone(KST_ZONE), isbn13);
            return;
        }

        aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
    }

    public void on5xx(Integer keyIdx) {
        if (keyIdx == null) return;
        aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_5XX_MS);
    }

    public void reset429OnSuccess(Integer keyIdx) {
        if (keyIdx == null) return;
        daily429Count.remove(keyIdx);
    }

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
