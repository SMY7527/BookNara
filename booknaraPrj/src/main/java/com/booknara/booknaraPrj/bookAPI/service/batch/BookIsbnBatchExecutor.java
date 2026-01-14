package com.booknara.booknaraPrj.bookAPI.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * [BookIsbnBatchExecutor]
 * 도서 수집 배치 작업의 실행 제어 및 중복 방지 전용 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchExecutor {

    private final BookIsbnBatchService batchService;

    /** * 실행 상태 플래그 (Lock)
     * - false: 대기 중, true: 현재 실행 중
     * - AtomicBoolean을 통해 멀티스레드 환경에서 원자적 상태 변경 보장
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 배치 실행 메인 진입점
     * @param trigger 실행 주체 (SCHEDULER, MANUAL 등)
     */
    public void execute(String trigger) {
        // [중복 실행 방지] 이미 실행 중(true)이면 즉시 종료, 아니면 true로 변경 후 진행
        if (!running.compareAndSet(false, true)) {
            log.warn("도서 수집 배치가 이미 실행 중입니다. (trigger={}, skip)", trigger);
            return;
        }

        long startMs = System.currentTimeMillis();
        try {
            log.info("▶ 도서 수집 배치 시작 (trigger={})", trigger);

            // 실제 비즈니스 로직 위임
            batchService.runBatch();

            log.info("▶ 도서 수집 배치 종료 (trigger={}, 소요시간={}ms)",
                    trigger, System.currentTimeMillis() - startMs);
        } catch (Exception e) {
            log.error("❌ 도서 수집 배치 중 오류 발생 (trigger={})", trigger, e);
        } finally {
            // 작업 성공/실패 여부와 관계없이 반드시 실행 플래그 해제
            running.set(false);
        }
    }

    /** 인자 없는 호출 시 'MANUAL' 트리거로 간주 */
    public void execute() {
        execute("MANUAL");
    }
}