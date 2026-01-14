package com.booknara.booknaraPrj.bookAPI.service.sync.aladin;

import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model.AladinCallResult;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.policy.AladinFetchPolicy;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.stop.AladinStopController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [AladinBookSyncService]
 * 알라딘 API를 통해 Staging 테이블(TEMP)의 도서 정보를 보강하는 실행 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AladinBookSyncService {

    private final BookBatchMapper batchMapper;
    private final AladinFetchPolicy fetchPolicy;
    private final AladinTempUpdateService tempUpdateService;
    private final AladinStopController stopController;
    private final PlatformTransactionManager txManager;

    /**
     * 전체 수집 루프: 처리할 대상이 없을 때까지 limit 단위로 계속 실행합니다.
     */
    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break; // 더 이상 처리할 대상이 없으면 종료
        }
        log.info("알라딘 동기화 루프가 종료되었습니다.");
    }

    /**
     * 단일 배치 실행: 지정된 limit만큼 ISBN을 가져와 순차적으로 보강 작업을 수행합니다.
     */
    public int syncOnce(int limit) {
        // [체크] 전역 스토퍼에 의해 정지된 상태라면 즉시 스킵
        if (stopController.isStoppedNow()) {
            log.warn("알라딘 동기화 스킵: 정지 상태 (~{})", stopController.stopUntilKstString());
            return 0;
        }

        // 보강 대상(ALADIN_RES_STATUS가 미시도/재시도인 데이터) 조회
        List<String> targetIsbnList = batchMapper.selectTempIsbnForAladin(limit);
        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("알라딘 동기화: 대상 데이터가 없습니다.");
            return 0;
        }

        // 각 ISBN 처리를 독립적인 트랜잭션으로 관리 (하나가 터져도 나머지는 저장되도록)
        TransactionTemplate tt = new TransactionTemplate(txManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int successCount = 0;

        for (String isbn13 : targetIsbnList) {
            // 루프 중간에 API 한도 초과 등으로 정지 플래그가 세워졌는지 수시로 확인
            if (stopController.isStoppedNow()) {
                log.warn("알라딘 동기화 중단: 실행 중 정지됨 (~{})", stopController.stopUntilKstString());
                return 0;
            }

            // 개별 ISBN 동기화 실행 (독립 트랜잭션)
            AladinCallResult r = tt.execute(status -> syncOneInternal(isbn13));
            if (r == null) continue;

            // 만약 결과가 '전체 정지' 신호라면 스토퍼 가동 후 루프 탈출
            if (r.isStopLoop()) {
                stopController.stopUntilTomorrow();
                log.warn("알라딘 일일 제한 도달 -> 내일 자정까지 정지 (에러코드={})", r.getErrorCode());
                return 0;
            }

            if (r.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                successCount++;
            }
        }

        log.info("알라딘 동기화 완료: 성공={}/전체={}", successCount, targetIsbnList.size());
        return targetIsbnList.size();
    }

    /**
     * 개별 ISBN에 대한 실제 동기화 비즈니스 로직
     */
    private AladinCallResult syncOneInternal(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            if (stopController.isStoppedNow()) {
                return AladinCallResult.stopForToday("STOP_FLAG");
            }

            // 1. API 호출 (재시도 정책 포함)
            AladinCallResult result = fetchPolicy.fetchWithRetry(isbn13);

            if (result.isStopLoop()) {
                return result;
            }

            // 2. 메타 정보 업데이트 (성공/실패 여부와 관계없이 시도 시각 및 결과 코드 기록)
            tempUpdateService.updateMeta(isbn13, triedAt, result.getStatus());

            // 3. 호출 성공 시 실제 데이터 반영
            if (result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                // 필수 데이터(이미지 등)가 누락되었는지 검증하며 업데이트
                boolean essentialOk = tempUpdateService.updateDataIfAnyAndCheckEssential(
                        isbn13, result.getResponse(), triedAt
                );

                // 필수 데이터가 없다면 더 이상 재시도하지 않도록 상태 격하(NonRetry)
                if (!essentialOk) {
                    tempUpdateService.markNonRetryBecauseMissingEssential(isbn13, triedAt);
                    return AladinCallResult.nonRetryFail("MISSING_ALADIN_ESSENTIAL");
                }
            }

            // 4. READY 판정 및 해시 갱신 (마스터 이관 준비)
            tempUpdateService.tryMarkReadyAndHash(isbn13);

            return result;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("알라딘 동기화 인터럽트 발생: isbn13={}", isbn13);
            tempUpdateService.safeUpdateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            return AladinCallResult.retryableFail("INTERRUPTED");

        } catch (Exception ex) {
            log.warn("알라딘 동기화 예상치 못한 오류: isbn13={}", isbn13, ex);
            tempUpdateService.safeUpdateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            return AladinCallResult.retryableFail("UNEXPECTED");

        } finally {
            // API 서버 부하 방지를 위한 미세 대기 (Throttling)
            try { Thread.sleep(120); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}