package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.stop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class AladinStopController {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5;

    private volatile long stopUntilEpochMs = 0L;

    public boolean isStoppedNow() {
        return stopUntilEpochMs > System.currentTimeMillis();
    }

    public void stopUntilTomorrow() {
        stopUntilEpochMs = getNextScheduledTime();
    }

    public String stopUntilKstString() {
        if (stopUntilEpochMs <= 0) return "N/A";
        return Instant.ofEpochMilli(stopUntilEpochMs).atZone(KST_ZONE).toString();
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
