package com.booknara.booknaraPrj.bookAPI.service.batch.util;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class BatchScheduleUtil {

    private BatchScheduleUtil() {
    }

    /**
     * KST 기준 "다음날 00:00 + delayMinutes" epoch millis
     */
    public static long nextKstMidnightPlusMinutes(long delayMinutes) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        return ZonedDateTime.now(kst)
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(kst)
                .plusMinutes(delayMinutes)
                .toInstant()
                .toEpochMilli();
    }
}