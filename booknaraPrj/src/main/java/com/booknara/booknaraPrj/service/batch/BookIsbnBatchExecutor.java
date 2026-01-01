package com.booknara.booknaraPrj.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchExecutor {

    private final BookIsbnBatchService batchService;

    // 중복 실행 방지 (스케줄/수동 공용)
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    /**
     * 배치 실행 (자동/수동 공용 엔트리포인트)
     */
    public void execute() {
        if (!RUNNING.compareAndSet(false, true)) {
            log.warn("Book ISBN batch is already running. Skip execution.");
            return;
        }

        try {
            log.info("▶ Book ISBN batch execution started");
            batchService.runBatch();
            log.info("▶ Book ISBN batch execution finished");
        } catch (Exception e) {
            log.error("❌ Book ISBN batch execution failed", e);
        } finally {
            RUNNING.set(false);
        }
    }
}
