package com.booknara.booknaraPrj.bookAPI.service.sync.naver;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.bookAPI.domain.ResponseStatus;
import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.bookAPI.service.policy.TempReadyPolicy;
import com.booknara.booknaraPrj.bookAPI.service.sync.naver.mapper.NaverTempMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NaverTempUpdateService {

    private final BookBatchMapper batchMapper;
    private final TempReadyPolicy readyPolicy;
    private final NaverTempMapper naverTempMapper;

    public void updateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        batchMapper.updateTempNaverMeta(isbn13, triedAt, status.getCode());
    }

    public void updateDataIfAny(String isbn13, NaverResponse response, LocalDateTime triedAt) {
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) return;

        BookIsbnTempDTO tempUpdate = naverTempMapper.toTempUpdateDto(isbn13, response, triedAt);
        batchMapper.updateTempFromNaver(tempUpdate);
    }

    public void tryMarkReady(String isbn13) {
        BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
        if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
            batchMapper.markTempReady(isbn13);
        }
    }
}
