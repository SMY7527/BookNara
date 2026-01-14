package com.booknara.booknaraPrj.bookAPI.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * [BookIsbnBatchScheduler]
 * 정기적인 도서 데이터 수집 배치를 실행하기 위한 스케줄링 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchScheduler {

    private final BookIsbnBatchExecutor executor;

    /**
     * 매일 새벽 3시(KST) 정기 배치 실행
     * - 트래픽이 가장 낮은 시간대에 대량 수집 및 이관 작업 수행
     * - Cron 표현식: 초 분 시 일 월 요일
     */

    @Scheduled(cron = "0 00 18 * * *", zone = "Asia/Seoul")
    public void runNightlyBatch() {
        log.info("⏰ 정기 도서 수집 배치가 예약된 시간에 트리거되었습니다. (KST 03:00)");

        // 실제 실행 제어는 Executor에 위임 (중복 실행 방지 로직 활용)
        executor.execute("SCHEDULE");
    }
}