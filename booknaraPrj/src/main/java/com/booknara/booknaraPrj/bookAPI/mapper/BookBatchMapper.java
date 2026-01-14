package com.booknara.booknaraPrj.bookAPI.mapper;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * [BookBatchMapper]
 * 도서 메타데이터 수집 배치 프로세스의 DB 접근 제어 레이어
 */
@Mapper
public interface BookBatchMapper {

    // --- STEP 0. 원천 데이터 초기 적재 (Seed Data) ---
    /** 정보나루 등 외부 원천에서 수집한 ISBN 목록을 Staging 테이블에 최초 저장 */
    int insertBookIsbnTemp(@Param("list") List<BookIsbnTempDTO> list);

    // --- STEP 1. NAVER 데이터 보강 (Enrichment) ---
    /** 네이버 수집이 필요한 대상(미시도 또는 재시도 가능 실패) 조회 */
    List<String> selectTempIsbnForNaver(@Param("limit") int limit);

    /** 네이버 API 호출 시각 및 응답 상태(성공/실패 코드) 업데이트 */
    int updateTempNaverMeta(@Param("isbn13") String isbn13,
                            @Param("naverFetchedAt") LocalDateTime naverFetchedAt,
                            @Param("naverResStatus") int naverResStatus);

    /** 네이버로부터 수집한 저자명, 설명, 이미지 URL 반영 */
    int updateTempFromNaver(BookIsbnTempDTO dto);

    // --- STEP 2. ALADIN 데이터 보강 (Enrichment) ---
    /** 알라딘 수집이 필요한 대상 조회 */
    List<String> selectTempIsbnForAladin(@Param("limit") int limit);

    /** 알라딘 API 호출 시각 및 응답 상태 업데이트 */
    int updateTempAladinMeta(@Param("isbn13") String isbn13,
                             @Param("aladinFetchedAt") LocalDateTime aladinFetchedAt,
                             @Param("aladinResStatus") int aladinResStatus);

    /** 알라딘으로부터 수집한 출판일, 장르ID, 고화질 이미지 반영 */
    int updateTempFromAladin(BookIsbnTempDTO dto);

    // --- STEP 3. 상태 관리 및 마스터 테이블 이관 (Merge) ---
    /** 데이터 변경 감지용 해시값 기록 */
    int updateTempDataHash(@Param("isbn13") String isbn13, @Param("dataHash") String dataHash);

    /** 모든 필수 정보가 충족된 데이터를 '이관 대기(READY)' 상태로 변경 */
    int markTempReady(@Param("isbn13") String isbn13);

    /** READY 상태 취소 및 대기(PENDING) 상태로 롤백 */
    int rollbackTempReadyToPending(@Param("isbn13") String isbn13);

    /** 마스터 테이블 반영 완료 후 '이관 완료(MERGED)' 상태로 변경 */
    int markTempMerged(@Param("isbn13") String isbn13);

    /** 검증 및 가공을 위한 단건 조회 */
    BookIsbnTempDTO selectTempByIsbn13(@Param("isbn13") String isbn13);

    /** 마스터 테이블로 이관할 대상(READY 상태) ISBN 목록 조회 */
    List<String> selectTempIsbnForMerge(@Param("limit") int limit);

    /** Staging 데이터를 마스터 테이블(BOOK_ISBN)에 최종 업서트(INSERT or UPDATE) */
    int upsertBookIsbnFromTemp(@Param("isbn13") String isbn13);
}