package com.booknara.booknaraPrj.bookAPI.service.batch;

import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.AladinBookSyncService;
import com.booknara.booknaraPrj.bookAPI.service.sync.infonaru.InfoNaruService;
import com.booknara.booknaraPrj.bookAPI.service.sync.naver.NaverBookSyncService;
import com.booknara.booknaraPrj.bookAPI.service.temp.TempMergeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * [BookIsbnBatchService]
 * 도서 데이터 수집 파이프라인의 전체 실행 순서와 실패 정책을 관리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookIsbnBatchService {

    private final InfoNaruService infoNaruService;
    private final NaverBookSyncService naverBookSyncService;
    private final AladinBookSyncService aladinBookSyncService;
    private final TempMergeService tempMergeService;

    /**
     * 도서 수집 전체 공정 실행 (4단계 파이프라인)
     */
    public void runBatch() {
        log.info("📚 도서 ISBN 배치 파이프라인 시작");

        // [STEP 1/4] 기초 데이터 적재 (Seed)
        // 정보나루에서 ISBN 기반 도서 목록 수집 (실패 시 파이프라인 즉시 중단)
        try {
            log.info("[1/4] 정보나루 수집 시작");
            infoNaruService.importTop100k();
            log.info("[1/4] 정보나루 수집 완료");
        } catch (Exception e) {
            log.error("❌ [1/4] 기초 데이터 수집 실패로 전체 공정을 중단합니다.", e);
            return;
        }

        // [STEP 2/4] 네이버 데이터 보강 (Enrichment)
        // 저자명, 이미지, 상세설명 보강 (실패 시 로그 기록 후 다음 단계 진행)
        try {
            log.info("[2/4] 네이버 데이터 보강 시작");
            naverBookSyncService.syncLoop(200);
            log.info("[2/4] 네이버 데이터 보강 완료");
        } catch (Exception e) {
            log.error("❌ [2/4] 네이버 보강 실패 (공정 유지)", e);
        }

        // [STEP 3/4] 알라딘 데이터 보강 (Enrichment)
        // 출판일, 장르, 고화질 이미지 보강 (실패 시 로그 기록 후 다음 단계 진행)
        try {
            log.info("[3/4] 알라딘 데이터 보강 시작");
            aladinBookSyncService.syncLoop(200);
            log.info("[3/4] 알라딘 데이터 보강 완료");
        } catch (Exception e) {
            log.error("❌ [3/4] 알라딘 보강 실패 (공정 유지)", e);
        }

        // [STEP 4/4] 운영 테이블 이관 (Merge)
        // 수집 완료(READY)된 데이터를 최종 운영 DB(BOOK_ISBN)에 반영
        try {
            log.info("[4/4] 마스터 테이블 이관 시작");
            tempMergeService.mergeLoop(200);
            log.info("[4/4] 마스터 테이블 이관 완료");
        } catch (Exception e) {
            log.error("❌ [4/4] 이관 공정 실패", e);
        }

        log.info("✅ 도서 ISBN 전체 배치 파이프라인 종료");
    }
}