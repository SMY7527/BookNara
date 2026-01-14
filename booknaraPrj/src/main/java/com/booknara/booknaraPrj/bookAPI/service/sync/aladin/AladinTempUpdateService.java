package com.booknara.booknaraPrj.bookAPI.service.sync.aladin;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.bookAPI.service.batch.hash.BookIsbnHash;
import com.booknara.booknaraPrj.bookAPI.service.policy.TempReadyPolicy;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.mapper.AladinTempMapper;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.parser.AladinPayloadParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * [AladinTempUpdateService]
 * 알라딘 수집 데이터를 TEMP 테이블에 반영하고, 데이터 품질 및 상태(READY)를 관리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AladinTempUpdateService {

    private final BookBatchMapper batchMapper;
    private final TempReadyPolicy readyPolicy;
    private final AladinTempMapper tempMapper;
    private final AladinPayloadParser payloadParser;

    /** 예외 발생 시 무시하는 안전한 메타데이터 업데이트 (주로 finally 블록에서 사용) */
    public void safeUpdateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        try {
            batchMapper.updateTempAladinMeta(isbn13, triedAt, status.getCode());
        } catch (Exception ignore) {}
    }

    /** 알라딘 API 호출 시도 시각과 응답 상태 코드를 TEMP 테이블에 기록 */
    public void updateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        batchMapper.updateTempAladinMeta(isbn13, triedAt, status.getCode());
    }

    /**
     * 알라딘 응답 데이터를 TEMP 테이블에 업데이트하고 필수 필드 충족 여부를 확인합니다.
     * @return true: 필수 데이터가 모두 채워짐, false: 필수 데이터 일부 누락
     */
    public boolean updateDataIfAnyAndCheckEssential(String isbn13, AladinResponse response, LocalDateTime triedAt) {
        if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
            return true;
        }

        // 1) API 응답을 DTO로 매핑하여 DB 업데이트
        BookIsbnTempDTO tempUpdate = tempMapper.toTempUpdateDto(isbn13, response, triedAt);
        batchMapper.updateTempFromAladin(tempUpdate);

        // 2) 업데이트 후 전체 데이터를 재조회하여 필수 요건 확인
        BookIsbnTempDTO afterUpdate = batchMapper.selectTempByIsbn13(isbn13);
        return hasAladinEssential(afterUpdate);
    }

    /** 필수 데이터 누락 시 '재시도 불가(NONRETRY_FAIL)'로 마킹하여 무한 재시도 방지 */
    public void markNonRetryBecauseMissingEssential(String isbn13, LocalDateTime triedAt) {
        batchMapper.updateTempAladinMeta(isbn13, triedAt, ResponseStatus.NONRETRY_FAIL.getCode());
        payloadParser.clearParseFail(isbn13); // 파싱 실패 카운트 초기화
        log.warn("알라딘 수집 성공했으나 필수 데이터(장르/날짜/커버) 누락으로 차단: isbn13={}", isbn13);
    }

    /**
     * 데이터가 운영 테이블로 이관될 준비(READY)가 되었는지 판단하고,
     * 준비되었다면 변경 감지용 해시(Hash) 계산 및 상태 승격을 수행합니다.
     */
    public void tryMarkReadyAndHash(String isbn13) {
        BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);

        // 모든 외부 API(정보나루/네이버/알라딘) 데이터가 정책에 부합하는지 확인
        if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
            // 데이터 지문(Hash) 생성: 운영 테이블 이관 시 변경 사항이 있는 데이터만 업데이트하기 위함
            String newHash = BookIsbnHash.compute(mergedTemp);
            batchMapper.updateTempDataHash(isbn13, newHash);

            // STATUS_CD를 1(READY)로 변경하여 병합 대상에 포함시킴
            batchMapper.markTempReady(isbn13);
        }
    }

    /** 알라딘에서 반드시 가져와야 하는 '3대 필수 데이터' 존재 여부 검증 */
    private boolean hasAladinEssential(BookIsbnTempDTO t) {
        if (t == null) return false;

        boolean hasGenre = (t.getGenreId() != null && t.getGenreId() > 0);
        boolean hasPubdate = (t.getPubdate() != null);
        boolean hasCover = (t.getAladinImageBig() != null && !t.getAladinImageBig().trim().isEmpty());

        return hasGenre && hasPubdate && hasCover;
    }
}