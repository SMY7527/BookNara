package com.booknara.booknaraPrj.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchScheduler {

    private final BookIsbnBatchService batchService;

    // 중복 실행 방지
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    /**
     * 매일 새벽 3시 실행 (KST 기준)
     */
    @Scheduled(cron = "0 55 7 * * *")
    public void runNightlyBatch() {

        if (!RUNNING.compareAndSet(false, true)) {
            log.warn("Batch already running. Skip this schedule.");
            return;
        }

        try {
            log.info(" Nightly book ISBN batch started");
            batchService.runBatch();
            log.info(" Nightly book ISBN batch finished");
        } catch (Exception e) {
            log.error("❌ Nightly book ISBN batch failed", e);
        } finally {
            RUNNING.set(false);
        }
    }
}

