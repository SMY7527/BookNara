package com.booknara.booknaraPrj.bookAPI.mapper;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BookBatchMapper {

    // =========================================================
    // STEP 0. TEMP 초기 적재 (InfoNaru 등 외부 원천)
    // =========================================================

    /**
     * STEP 0
     * 정보나루/외부 원천에서 수집한 ISBN을 TEMP 테이블에 최초 적재
     * - STATUS_CD = 0 (NOTREADY)
     * - *_RES_STATUS = 0 (NOT_TRIED)
     */
    int insertBookIsbnTemp(@Param("list") List<BookIsbnTempDTO> list);


    // =========================================================
    // STEP 1. NAVER 보강
    // =========================================================

    /**
     * STEP 1-1
     * 네이버 보강 대상 ISBN 조회
     * - STATUS_CD = 0 (NOTREADY)
     * - NAVER_RES_STATUS IN (0: NOT_TRIED, 3: RETRYABLE_FAIL)
     */
    List<String> selectTempIsbnForNaver(@Param("limit") int limit);

    /**
     * STEP 1-2
     * 네이버 메타 업데이트 (항상 실행)
     * - NAVER_FETCHED_AT
     * - NAVER_RES_STATUS (응답 결과 상태코드)
     */
    int updateTempNaverMeta(@Param("isbn13") String isbn13,
                            @Param("naverFetchedAt") LocalDateTime naverFetchedAt,
                            @Param("naverResStatus") int naverResStatus);

    /**
     * STEP 1-3
     * 네이버 데이터 업데이트
     * - SUCCESS_WITH_DATA(1) 일 때만 실행
     * - AUTHORS / DESCRIPTION / NAVER_IMAGE
     */
    int updateTempFromNaver(BookIsbnTempDTO dto);


    // =========================================================
    // STEP 2. ALADIN 보강
    // =========================================================

    /**
     * STEP 2-1
     * 알라딘 보강 대상 ISBN 조회
     * - STATUS_CD = 0 (NOTREADY)
     * - ALADIN_RES_STATUS IN (0: NOT_TRIED, 3: RETRYABLE_FAIL)
     */
    List<String> selectTempIsbnForAladin(@Param("limit") int limit);

    /**
     * STEP 2-2
     * 알라딘 메타 업데이트 (항상 실행)
     * - ALADIN_FETCHED_AT
     * - ALADIN_RES_STATUS (응답 결과 상태코드)
     */
    int updateTempAladinMeta(@Param("isbn13") String isbn13,
                             @Param("aladinFetchedAt") LocalDateTime aladinFetchedAt,
                             @Param("aladinResStatus") int aladinResStatus);

    /**
     * STEP 2-3
     * 알라딘 데이터 업데이트
     * - SUCCESS_WITH_DATA(1) 일 때만 실행
     * - PUBDATE / GENRE_ID / ALADIN_IMAGE_BIG / DESCRIPTION
     */
    int updateTempFromAladin(BookIsbnTempDTO dto);


    // =========================================================
    // STEP 3. READY 판정 / HASH / MERGE
    // =========================================================

    /**
     * STEP 3-1
     * TEMP 데이터 해시 업데이트
     * - READY 판정 직전 또는 직후
     * - 외부 API 데이터 변경 감지용
     */
    int updateTempDataHash(@Param("isbn13") String isbn13,
                           @Param("dataHash") String dataHash);

    /**
     * STEP 3-2
     * READY 상태로 승격
     * - STATUS_CD = 1
     * - 네이버/알라딘/필수 컬럼 충족 시
     */
    int markTempReady(@Param("isbn13") String isbn13);


    /**
     * STEP 3-2.5
     * READY -> PENDING 롤백
     */
    int rollbackTempReadyToPending(@Param("isbn13") String isbn13);


    /**
     * STEP 3-3
     * TEMP → BOOK_ISBN 병합 완료 표시
     * - STATUS_CD = 2 (MERGED)
     */
    int markTempMerged(@Param("isbn13") String isbn13);

    /**
     * STEP 3-4
     * TEMP 단건 조회
     * - READY 판정 / HASH 계산 / 병합 전 검증용
     */
    BookIsbnTempDTO selectTempByIsbn13(@Param("isbn13") String isbn13);


    /**
     * STEP 3-5
     * READY 상태의 ISBN 조회 (병합 대상)
     */
    List<String> selectTempIsbnForMerge(@Param("limit") int limit);

    /**
     * STEP 3-6
     * TEMP(READY) → BOOK_ISBN 업서트
     * - 최종 운영 테이블 반영
     */
    int upsertBookIsbnFromTemp(@Param("isbn13") String isbn13);
}
