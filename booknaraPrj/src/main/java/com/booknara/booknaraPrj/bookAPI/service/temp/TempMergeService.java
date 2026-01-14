package com.booknara.booknaraPrj.bookAPI.service.temp;

import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [TempMergeService]
 * 검증이 완료된(READY) Staging 데이터를 운영 테이블(BOOK_ISBN)로 병합(Upsert)하는 서비스입니다.
 * ETL 파이프라인의 최종 적재(Load) 단계를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TempMergeService {

    private final BookBatchMapper batchMapper;

    /**
     * READY(상태 1)인 데이터를 지정된 건수(limit)만큼 가져와 운영 테이블에 반영합니다.
     * 반영 성공 시 해당 데이터의 상태를 MERGED(상태 2)로 변경합니다.
     * * @return 성공 여부와 관계없이 "시도한 전체 건수" (루프 제어용)
     */
    public int mergeOnce(int limit) {
        // 1. 이관 준비가 완료된(READY) ISBN 목록 조회
        List<String> readyIsbnList = batchMapper.selectTempIsbnForMerge(limit);
        if (readyIsbnList == null || readyIsbnList.isEmpty()) {
            log.info("Merge 공정: 이관할 READY 상태의 데이터가 없습니다.");
            return 0;
        }

        int successCount = 0;
        int processedCount = 0;

        for (String isbn13 : readyIsbnList) {
            processedCount++;
            // 2. 단일 건별 이관 실행
            if (mergeOne(isbn13)) successCount++;
        }

        log.info("Merge 결과: 처리 시도={}, 성공={}/{}", processedCount, successCount, readyIsbnList.size());

        // 처리한 개수를 반환하여, 일부가 실패하더라도 다음 청크(Chunk)로 넘어갈 수 있도록 설계됨
        return processedCount;
    }

    /**
     * 더 이상 이관할 READY 데이터가 없을 때까지 mergeOnce를 반복 실행합니다.
     */
    public void mergeLoop(int limit) {
        while (true) {
            int processed = mergeOnce(limit);
            if (processed == 0) break; // 더 이상 처리할 대상이 없으면 루프 종료
        }
        log.info("전체 데이터 이관 공정(MergeLoop)이 완료되었습니다.");
    }

    /**
     * 개별 ISBN 1건에 대한 이관 및 상태 변경을 수행합니다.
     * 독립 트랜잭션(REQUIRES_NEW)을 사용하여 특정 건의 실패가 다른 건에 영향을 주지 않도록 보호합니다.
     *
     * @return 이관 성공 여부
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean mergeOne(String isbn13) {
        try {
            // 1) 운영 테이블로 Upsert (Insert or Update) 수행
            // MyBatis의 <insert id="upsert..."> 로직에 의해 데이터가 반영됨
            int affected = batchMapper.upsertBookIsbnFromTemp(isbn13);

            // 2) [방어 로직] SQL 조건에 의해 업데이트가 발생하지 않은 경우 (예: genre_id 누락 등)
            if (affected == 0) {
                // READY 상태지만 실제 병합 조건에 미달하는 데이터는 다시 PENDING(0)으로 되돌림
                // 이 처리가 없으면 매 배치마다 해당 데이터가 READY로 조회되어 무한 루프를 유발함
                log.warn("Merge 스킵: 업서트 조건 미달로 인해 PENDING으로 롤백합니다. isbn13={}", isbn13);
                batchMapper.rollbackTempReadyToPending(isbn13);
                return false;
            }

            // 3) 이관 성공 시 상태를 MERGED(2)로 변경하여 공정 완료 처리
            batchMapper.markTempMerged(isbn13);
            return true;

        } catch (Exception e) {
            log.warn("Merge 실패: 데이터베이스 오류 발생. isbn13={}", isbn13, e);
            // 예외 발생 시에는 READY 상태를 유지하여 다음 배치 때 재시도할 수 있도록 함
            return false;
        }
    }
}