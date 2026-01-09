package com.booknara.booknaraPrj.bookAPI.service.sync.naver;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.bookAPI.service.sync.naver.model.NaverCallResult;
import com.booknara.booknaraPrj.bookAPI.service.sync.naver.policy.NaverFetchPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverBookSyncService {

    private final BookBatchMapper batchMapper;
    private final NaverFetchPolicy fetchPolicy;
    private final NaverTempUpdateService tempUpdateService;

    public int syncOnce(int limit) {
        List<String> targetIsbnList = batchMapper.selectTempIsbnForNaver(limit);

        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("naver sync: no targets");
            return 0;
        }

        int successCount = 0;
        for (String isbn13 : targetIsbnList) {
            if (syncOne(isbn13)) {
                successCount++;
            }
        }

        log.info("naver sync: success={}/{}", successCount, targetIsbnList.size());
        return targetIsbnList.size();
    }

    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break;
        }
        log.info("naver syncLoop finished");
    }

    /**
     * 네이버 API 보강 (단일 ISBN)
     * - triedAt + resStatus 메타는 항상 기록
     * - 성공+데이터 있을 때만 데이터 업데이트
     * - READY 판정은 최종 temp 기준
     */
    @Transactional
    public boolean syncOne(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            NaverCallResult result = fetchPolicy.fetchWithRetry(isbn13);

            // 1) 메타 업데이트는 항상
            tempUpdateService.updateMeta(isbn13, triedAt, result.getStatus());

            // 2) 데이터 업데이트는 성공+데이터일 때만
            if (result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                tempUpdateService.updateDataIfAny(isbn13, result.getResponse(), triedAt);
            }

            // 3) READY 판정
            tempUpdateService.tryMarkReady(isbn13);

            return result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("naver sync interrupted isbn13={}", isbn13);

            return false;

        } catch (Exception ex) {
            log.warn("naver sync failed (unexpected) isbn13={}", isbn13, ex);

            try {
                tempUpdateService.updateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            } catch (Exception ignore) {}

            return false;

        } finally {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
