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

@Service
@RequiredArgsConstructor
@Slf4j
public class AladinBookSyncService {

    private final BookBatchMapper batchMapper;
    private final AladinFetchPolicy fetchPolicy;
    private final AladinTempUpdateService tempUpdateService;
    private final AladinStopController stopController;
    private final PlatformTransactionManager txManager;

    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break;
        }
        log.info("aladin syncLoop finished");
    }

    public int syncOnce(int limit) {
        if (stopController.isStoppedNow()) {
            log.warn("aladin sync skipped: stoppedUntil={}", stopController.stopUntilKstString());
            return 0;
        }

        List<String> targetIsbnList = batchMapper.selectTempIsbnForAladin(limit);
        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("aladin sync: no targets");
            return 0;
        }

        TransactionTemplate tt = new TransactionTemplate(txManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int successCount = 0;

        for (String isbn13 : targetIsbnList) {
            if (stopController.isStoppedNow()) {
                log.warn("aladin sync interrupted mid-run: stoppedUntil={}", stopController.stopUntilKstString());
                return 0;
            }

            AladinCallResult r = tt.execute(status -> syncOneInternal(isbn13));
            if (r == null) continue;

            if (r.isStopLoop()) {
                stopController.stopUntilTomorrow();
                log.warn("aladin stopForToday detected -> stopUntil={} errorCode={}",
                        stopController.stopUntilKstString(), r.getErrorCode());
                return 0;
            }

            if (r.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                successCount++;
            }
        }

        log.info("aladin sync: success={}/{}", successCount, targetIsbnList.size());
        return targetIsbnList.size();
    }

    private AladinCallResult syncOneInternal(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            if (stopController.isStoppedNow()) {
                return AladinCallResult.stopForToday("STOP_FLAG");
            }

            AladinCallResult result = fetchPolicy.fetchWithRetry(isbn13);

            if (result.isStopLoop()) {
                return result;
            }

            // 1) meta always
            tempUpdateService.updateMeta(isbn13, triedAt, result.getStatus());

            // 2) data update if any
            if (result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                boolean essentialOk = tempUpdateService.updateDataIfAnyAndCheckEssential(
                        isbn13, result.getResponse(), triedAt
                );

                if (!essentialOk) {
                    tempUpdateService.markNonRetryBecauseMissingEssential(isbn13, triedAt);
                    return AladinCallResult.nonRetryFail("MISSING_ALADIN_ESSENTIAL");
                }
            }

            // 3) READY + HASH (기존 유지)
            tempUpdateService.tryMarkReadyAndHash(isbn13);

            return result;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("aladin sync interrupted isbn13={}", isbn13);

            tempUpdateService.safeUpdateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            return AladinCallResult.retryableFail("INTERRUPTED");

        } catch (Exception ex) {
            log.warn("aladin sync failed (unexpected) isbn13={}", isbn13, ex);

            tempUpdateService.safeUpdateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            return AladinCallResult.retryableFail("UNEXPECTED");

        } finally {
            try {
                Thread.sleep(120);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
