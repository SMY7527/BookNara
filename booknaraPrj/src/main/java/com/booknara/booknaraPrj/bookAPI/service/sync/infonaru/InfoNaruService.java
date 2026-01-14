package com.booknara.booknaraPrj.bookAPI.service.sync.infonaru;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * [InfoNaruService]
 * 정보나루 API를 통해 대량의 초기 도서 데이터를 수집하는 서비스입니다.
 * 전체 파이프라인 중 '데이터 원천 확보' 단계를 총괄합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfoNaruService {

    private final InfoNaruPageImportService pageImportService;

    /**
     * 인기 도서 상위 10만 권의 메타데이터를 시스템에 적재합니다.
     * 전략: 페이지당 1만 건씩, 총 10페이지를 순차적으로 호출하여 10만 건 확보
     */
    public void importTop100k() {
        int pageSize = 10_000; // API가 허용하는 최대 수준의 페이지 크기 설정
        int totalPages = 10;   // 목표 수량(10만) 달성을 위한 전체 페이지 수

        // [이미지: 순차적 데이터 수집 및 배치 처리 흐름]
        //
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            // 개별 페이지 수집 서비스 호출
            boolean success = pageImportService.importOnePage(pageNo, pageSize);

            // [상태 기반 제어] 응답이 0건이거나 API 호출에 실패할 경우,
            // 더 이상의 시도는 무의미하므로 즉시 루프를 중단하여 시스템 리소스 보호
            if (!success) {
                log.warn("정보나루 수집 중단: 데이터 없음 또는 실패 (pageNo={})", pageNo);
                break;
            }
        }

        log.info("▶ 정보나루 상위 10만 권 수집 공정이 완료되었습니다.");
    }
}