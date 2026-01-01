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
     *
     * 단계:
     * 1) InfoNaru 기본 적재 (TEMP, NOTREADY)
     * 2) Naver 보강 (저자 / 설명 / 보조 이미지)
     * 3) Aladin 보강 (출간일 / 카테고리 / 대표 이미지)
     * 4) READY 데이터 운영 테이블 반영 (MERGE)
     *
     * ※ 각 단계는 내부적으로 limit/loop를 관리한다.
     */
    public void runBatch() {
        log.info("📚 Book ISBN batch pipeline started");

        // 1) InfoNaru → TEMP
        log.info("[1/4] InfoNaru import started");
        infoNaruService.importTop100k();
        log.info("[1/4] InfoNaru import finished");

        // 2) Naver 보강
        log.info("[2/4] Naver sync started");
        naverBookSyncService.syncLoop(200);
        log.info("[2/4] Naver sync finished");

        // 3) Aladin 보강
        log.info("[3/4] Aladin sync started");
        aladinBookSyncService.syncLoop(200);
        log.info("[3/4] Aladin sync finished");

        // 4) READY → 운영 반영
        log.info("[4/4] Merge started");
        tempMergeService.mergeLoop(200);
        log.info("[4/4] Merge finished");

        log.info("✅ Book ISBN batch pipeline finished");
    }
}
