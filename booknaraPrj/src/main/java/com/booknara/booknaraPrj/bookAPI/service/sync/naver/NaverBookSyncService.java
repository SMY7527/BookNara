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

/**
 * [NaverBookSyncService]
 * 네이버 검색 API를 활용하여 TEMP 테이블의 도서 데이터를 보강하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NaverBookSyncService {

    private final BookBatchMapper batchMapper;
    private final NaverFetchPolicy fetchPolicy;
    private final NaverTempUpdateService tempUpdateService;

    /**
     * 지정된 제한(limit) 건수만큼 네이버 데이터 동기화 작업을 수행합니다.
     * @param limit 한 번에 처리할 ISBN 개수
     * @return 실제 처리 대상 건수
     */
    public int syncOnce(int limit) {
        // 1. 네이버 보강이 필요한 대상 ISBN 목록 조회
        List<String> targetIsbnList = batchMapper.selectTempIsbnForNaver(limit);

        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("naver sync: 처리할 대상이 없습니다.");
            return 0;
        }

        int successCount = 0;
        for (String isbn13 : targetIsbnList) {
            // 2. 단일 건별로 보강 로직 수행
            if (syncOne(isbn13)) {
                successCount++;
            }
        }

        log.info("naver sync: 완료 (성공={}/{})", successCount, targetIsbnList.size());
        return targetIsbnList.size();
    }

    /**
     * 처리할 데이터가 없을 때까지 syncOnce를 반복 실행하는 전체 루프입니다.
     */
    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break; // 대상이 없으면 루프 종료
        }
        log.info("naver syncLoop가 최종 종료되었습니다.");
    }

    /**
     * 단일 ISBN에 대한 네이버 API 보강 로직을 수행합니다.
     * @param isbn13 보강 대상 도서 번호
     * @return 보강 성공 여부
     */
    @Transactional // 개별 도서 정보 업데이트의 원자성 보장
    public boolean syncOne(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            // 1) 네이버 API 호출 (재시도 정책 포함)
            NaverCallResult result = fetchPolicy.fetchWithRetry(isbn13);

            // 2) 메타데이터 업데이트: 성공/실패 여부와 관계없이 시도 시각 및 결과 코드는 항상 기록
            tempUpdateService.updateMeta(isbn13, triedAt, result.getStatus());

            // 3) 데이터 업데이트: API 호출 성공 및 실제 데이터가 존재할 때만 본문 보강
            if (result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA) {
                tempUpdateService.updateDataIfAny(isbn13, result.getResponse(), triedAt);
            }

            // 4) READY 판정: 모든 필수 조건이 충족되었는지 확인 후 상태 변경 시도
            tempUpdateService.tryMarkReady(isbn13);

            return result.getStatus() == ResponseStatus.SUCCESS_WITH_DATA;

        } catch (InterruptedException ie) {
            // 배치 중단 시 인터럽트 상태 복구
            Thread.currentThread().interrupt();
            log.warn("naver sync 중단됨: isbn13={}", isbn13);
            return false;

        } catch (Exception ex) {
            log.warn("naver sync 예상치 못한 오류 발생: isbn13={}", isbn13, ex);

            // 예외 발생 시 최소한의 메타데이터(실패 상태) 기록 시도
            try {
                tempUpdateService.updateMeta(isbn13, triedAt, ResponseStatus.RETRYABLE_FAIL);
            } catch (Exception ignore) {}

            return false;

        } finally {
            // [Throttling] 네이버 API 호출 정책 준수 및 서버 부하 방지를 위해 미세 지연(150ms) 추가
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}