package com.booknara.booknaraPrj.bookAPI.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchExecutor {
    /**
     * BookIsbnBatchExecutor
     *
     * - 도서 ISBN 배치 작업의 "실행 제어 전용" 컴포넌트
     * - 동일 배치의 중복 실행을 방지하기 위해 AtomicBoolean 기반 실행 락을 사용한다.
     *
     * 주요 책임:
     * 1) 배치가 이미 실행 중인지 검사
     * 2) 실행 중이면 스킵 처리 및 로그 기록
     * 3) 실행 시작/종료 시점 로깅 및 실행 시간 측정
     * 4) 실제 비즈니스 로직은 BookIsbnBatchService에 위임
     *
     * 설계 의도:
     * - 스케줄러, 수동 실행, 관리자 트리거 등
     *   다양한 실행 경로에서 동일한 배치 로직을 안전하게 호출하기 위함
     * - 실행 제어(락)와 배치 비즈니스 로직을 분리하여 책임을 명확히 한다.
     */


    private final BookIsbnBatchService batchService;

    /**
     * 배치 중복 실행 방지를 위한 실행 상태 플래그
     * - false : 실행 중 아님
     * - true  : 실행 중
     *
     * compareAndSet을 사용하여 멀티 스레드 환경에서도
     * 단 하나의 실행만 허용한다.
     */

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 배치 실행 진입점
     *
     * @param trigger 배치 실행 트리거 식별자
     *                (예: SCHEDULER, MANUAL, ADMIN_API 등)
     *
     * 동작 흐름:
     * 1) 이미 실행 중이면 즉시 종료 (중복 실행 방지)
     * 2) 실행 시작 로그 기록
     * 3) batchService.runBatch() 호출
     * 4) 실행 시간 측정 및 종료 로그 기록
     * 5) 예외 발생 시 로그 기록 후 실행 상태 해제
     */

    public void execute(String trigger) {
        if (!running.compareAndSet(false, true)) {
            log.warn("Book ISBN batch is already running. trigger={} -> skip", trigger);
            return;
        }

        long startMs = System.currentTimeMillis();
        try {
            log.info("▶ Book ISBN batch execution started. trigger={}", trigger);
            batchService.runBatch();
            log.info("▶ Book ISBN batch execution finished. trigger={} elapsedMs={}",
                    trigger, System.currentTimeMillis() - startMs);
        } catch (Exception e) {
            log.error("❌ Book ISBN batch execution failed. trigger={}", trigger, e);
        } finally {
            running.set(false);
        }
    }

    public void execute() {
        execute("MANUAL");
    }
}
