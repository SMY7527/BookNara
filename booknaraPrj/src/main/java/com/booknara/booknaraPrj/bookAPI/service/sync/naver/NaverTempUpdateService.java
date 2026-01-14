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

/**
 * [NaverTempUpdateService]
 * 네이버 수집 결과를 TEMP 테이블에 반영하고 도서 데이터의 상태 승격을 관리합니다.
 */
@Service
@RequiredArgsConstructor
public class NaverTempUpdateService {

    private final BookBatchMapper batchMapper;
    private final TempReadyPolicy readyPolicy;
    private final NaverTempMapper naverTempMapper;

    /**
     * 네이버 API 호출 시도 이력(시도 시각 및 결과 상태 코드)을 업데이트합니다.
     * 데이터 본문 수신 여부와 관계없이 호출 행위 자체를 기록하기 위해 사용됩니다.
     */
    public void updateMeta(String isbn13, LocalDateTime triedAt, ResponseStatus status) {
        batchMapper.updateTempNaverMeta(isbn13, triedAt, status.getCode());
    }

    /**
     * 네이버 API로부터 수신한 도서 상세 데이터(저자, 설명, 이미지 등)를 TEMP 테이블에 반영합니다.
     * @param response 네이버 API 응답 객체
     */
    public void updateDataIfAny(String isbn13, NaverResponse response, LocalDateTime triedAt) {
        // 응답 데이터가 유효한 경우에만 업데이트 수행
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) return;

        // API 응답을 DB 업데이트 규격(DTO)으로 변환 후 반영
        BookIsbnTempDTO tempUpdate = naverTempMapper.toTempUpdateDto(isbn13, response, triedAt);
        batchMapper.updateTempFromNaver(tempUpdate);
    }

    /**
     * 현재 TEMP 테이블에 축적된 데이터(정보나루+네이버+알라딘)가
     * 서비스에 노출될 수 있는 품질(READY)을 갖추었는지 검사하고 상태를 승격시킵니다.
     */
    public void tryMarkReady(String isbn13) {
        // 1. 현재 시점의 최신 TEMP 데이터 조회
        BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);

        // 2. 통합 품질 정책(TempReadyPolicy)에 부합하는지 확인
        if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
            // 3. 조건 충족 시 STATUS_CD를 1(READY)로 변경하여 운영 이관 대기 상태로 전환
            batchMapper.markTempReady(isbn13);
        }
    }
}