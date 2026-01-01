package com.booknara.booknaraPrj.service.temp;

import com.booknara.booknaraPrj.mapper.BookBatchMapper;
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
        for (String isbn13 : readyIsbnList) {
            if (mergeOne(isbn13)) {
                successCount++;
            }
        }

        log.info("merge: success={}/{}", successCount, readyIsbnList.size());
        return successCount;
    }

    /**
     * 더 이상 READY 대상이 없을 때까지 반복
     */
    public void mergeLoop(int limit) {
        while (true) {
            int merged = mergeOnce(limit);
            if (merged == 0) break;
        }
        log.info("mergeLoop finished");
    }

    /**
     * ISBN 1건 MERGE는 독립 트랜잭션으로 처리 (부분 성공 허용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean mergeOne(String isbn13) {
        try {
            // 1) TEMP -> BOOK_ISBN 업서트
            int affected = batchMapper.upsertBookIsbnFromTemp(isbn13);
            if (affected == 0) {
                // READY가 아니거나(조건절), TEMP가 없으면 0
                log.warn("merge skipped isbn13={} (not READY or not found)", isbn13);
                return false;
            }

            // 2) 성공 시 TEMP 상태 MERGED(2)
            batchMapper.markTempMerged(isbn13);
            return true;

        } catch (Exception e) {
            log.warn("merge failed isbn13={}", isbn13, e);
            // 여기서 status를 ERROR로 바꾸는 정책은 나중에 추가 가능
            return false;
        }
    }
}
