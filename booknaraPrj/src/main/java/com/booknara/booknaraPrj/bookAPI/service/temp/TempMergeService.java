package com.booknara.booknaraPrj.bookAPI.service.temp;

import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TempMergeService {

    private final BookBatchMapper batchMapper;

    /**
     * READY(1)인 TEMP를 limit 만큼 운영 테이블(BOOK_ISBN)에 반영하고
     * 성공하면 TEMP를 MERGED(2)로 변경한다.
     */
    public int mergeOnce(int limit) {
        List<String> readyIsbnList = batchMapper.selectTempIsbnForMerge(limit);
        if (readyIsbnList == null || readyIsbnList.isEmpty()) {
            log.info("merge: no READY targets");
            return 0;
        }

        int successCount = 0;
        int processedCount = 0;

        for (String isbn13 : readyIsbnList) {
            processedCount++;
            if (mergeOne(isbn13)) successCount++;
        }

        log.info("merge: processed={}, success={}/{}", processedCount, successCount, readyIsbnList.size());
        // ✅ 루프 제어용으로는 "처리한 개수" 리턴이 더 안정적(성공 0이어도 다음 페이지로 넘어가게)
        return processedCount;
    }

    /**
     * 더 이상 READY 대상이 없을 때까지 반복
     */
    public void mergeLoop(int limit) {
        while (true) {
            int processed = mergeOnce(limit);
            if (processed == 0) break;
        }
        log.info("mergeLoop finished");
    }

    /**
     * ISBN 1건 MERGE는 독립 트랜잭션으로 처리 (부분 성공 허용)
     *
     * ✅ 핵심:
     * - upsert가 0이면, READY인데도 merge가 안 되는 케이스(조건절에 걸림)가 존재할 수 있음
     * - 이런 ISBN을 STATUS_CD=1로 남겨두면 계속 READY 대상으로 반복 조회됨
     * - 최소 수정으로 STATUS_CD=0으로 되돌려 무한 반복 차단
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean mergeOne(String isbn13) {
        try {
            int affected = batchMapper.upsertBookIsbnFromTemp(isbn13);

            if (affected == 0) {
                // ✅ READY였지만 업서트 조건(genre_id>0 등)에 의해 제외된 케이스까지 포함 가능
                log.warn("merge skipped -> rollback READY to PENDING. isbn13={}", isbn13);
                batchMapper.rollbackTempReadyToPending(isbn13);
                return false;
            }

            batchMapper.markTempMerged(isbn13);
            return true;

        } catch (Exception e) {
            log.warn("merge failed isbn13={}", isbn13, e);
            // 실패는 READY를 그대로 두면 재시도 가능(원하면 여기서도 0으로 롤백 가능)
            return false;
        }
    }
}
