package com.booknara.booknaraPrj.service.batch;

import com.booknara.booknaraPrj.service.sync.aladin.AladinBookSyncService;
import com.booknara.booknaraPrj.service.sync.infonaru.InfoNaruService;
import com.booknara.booknaraPrj.service.sync.naver.NaverBookSyncService;
import com.booknara.booknaraPrj.service.temp.TempMergeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchService {

    private final InfoNaruService infoNaruService;
    private final NaverBookSyncService naverBookSyncService;
    private final AladinBookSyncService aladinBookSyncService;
    private final TempMergeService tempMergeService;

    /**
     * 도서 ISBN 데이터 전체 배치 파이프라인
     * 단계:
     * 1) InfoNaru 기본 적재 (TEMP, NOTREADY)
     * 2) Naver 보강
     * 3) Aladin 보강
     * 4) READY → 운영 반영(MERGE)
     *
     * 실패 정책:
     * - 1단계 실패: 중단 (파이프라인 기반 자체가 흔들림)
     * - 2/3/4단계 실패: 로그 남기고 다음 단계 진행 (부분 성공 누적)
     */
    public void runBatch() {
        log.info("📚 Book ISBN batch pipeline started");

        // ---------------------------------
        // [1/4] InfoNaru → TEMP
        // ---------------------------------
        try {
            log.info("[1/4] InfoNaru import started");
            infoNaruService.importTop100k();
            log.info("[1/4] InfoNaru import finished");
        } catch (Exception e) {
            // ✅ 이 단계가 무너지면 이후 단계 의미가 약함 -> 중단
            log.error("❌ [1/4] InfoNaru import failed -> stop pipeline", e);
            return;
        }

        // ---------------------------------
        // [2/4] Naver 보강
        // ---------------------------------
        try {
            log.info("[2/4] Naver sync started");
            naverBookSyncService.syncLoop(200);
            log.info("[2/4] Naver sync finished");
        } catch (Exception e) {
            log.error("❌ [2/4] Naver sync failed -> continue pipeline", e);
        }

        // ---------------------------------
        // [3/4] Aladin 보강
        // ---------------------------------
        try {
            log.info("[3/4] Aladin sync started");
            aladinBookSyncService.syncLoop(200);
            log.info("[3/4] Aladin sync finished");
        } catch (Exception e) {
            log.error("❌ [3/4] Aladin sync failed -> continue pipeline", e);
        }

        // ---------------------------------
        // [4/4] READY → 운영 반영 (MERGE)
        // ---------------------------------
        try {
            log.info("[4/4] Merge started");
            tempMergeService.mergeLoop(200);
            log.info("[4/4] Merge finished");
        } catch (Exception e) {
            log.error("❌ [4/4] Merge failed", e);
        }

        log.info("✅ Book ISBN batch pipeline finished");
    }
}
