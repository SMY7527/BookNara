package com.booknara.booknaraPrj.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchScheduler {

    private final BookIsbnBatchExecutor executor;

    /**
     * 매일 새벽 3시 실행 (KST)
     */
    // 초/분/시
    @Scheduled(cron = "0 00 3  * * *", zone = "Asia/Seoul")
    public void runNightlyBatch() {
        log.info("⏰ Nightly batch triggered (KST 03:00)");
        executor.execute("SCHEDULE");
    }
}
