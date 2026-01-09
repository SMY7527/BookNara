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

@Service
@RequiredArgsConstructor
@Slf4j
public class AladinTempUpdateService {

    private final BookBatchMapper batchMapper;
    private final TempReadyPolicy readyPolicy;
    private final AladinTempMapper tempMapper;
    private final AladinPayloadParser payloadParser;

    public void safeUpdateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        try {
            batchMapper.updateTempAladinMeta(isbn13, triedAt, status.getCode());
        } catch (Exception ignore) {}
    }

    public void updateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        batchMapper.updateTempAladinMeta(isbn13, triedAt, status.getCode());
    }

    /**
     * - SUCCESS_WITH_DATA면 temp 업데이트
     * - 업데이트 후 essential(genre/pubdate/cover) 없으면 NONRETRY_FAIL(4)로 내려 무한루프 차단
     *
     * @return essential 충족 여부
     */
    public boolean updateDataIfAnyAndCheckEssential(String isbn13, AladinResponse response, LocalDateTime triedAt) {
        if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
            return true;
        }

        BookIsbnTempDTO tempUpdate = tempMapper.toTempUpdateDto(isbn13, response, triedAt);
        batchMapper.updateTempFromAladin(tempUpdate);

        BookIsbnTempDTO afterUpdate = batchMapper.selectTempByIsbn13(isbn13);
        return hasAladinEssential(afterUpdate);
    }

    public void markNonRetryBecauseMissingEssential(String isbn13, LocalDateTime triedAt) {
        batchMapper.updateTempAladinMeta(isbn13, triedAt, ResponseStatus.NONRETRY_FAIL.getCode());
        payloadParser.clearParseFail(isbn13);
        log.warn("aladin success-but-missing-essential -> NONRETRY_FAIL(4) isbn13={}", isbn13);
    }


    public void tryMarkReadyAndHash(String isbn13) {
        BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
        if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
            String newHash = BookIsbnHash.compute(mergedTemp);
            batchMapper.updateTempDataHash(isbn13, newHash);
            batchMapper.markTempReady(isbn13);
        }
    }

    private boolean hasAladinEssential(BookIsbnTempDTO t) {
        if (t == null) return false;

        boolean hasGenre = (t.getGenreId() != null && t.getGenreId() > 0);
        boolean hasPubdate = (t.getPubdate() != null);
        boolean hasCover = (t.getAladinImageBig() != null && !t.getAladinImageBig().trim().isEmpty());

        return hasGenre && hasPubdate && hasCover;
    }
}
