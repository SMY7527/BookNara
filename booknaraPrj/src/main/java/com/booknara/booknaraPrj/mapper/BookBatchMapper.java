package com.booknara.booknaraPrj.mapper;

import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookBatchMapper {
    // 도서 목록을 일괄 INSERT 하기 위한 MyBatis 매퍼
    // 배치 적재(1만 건 단위)에서 사용
    int insertBookIsbnTemp(@Param("list") List<BookIsbnTempDTO> list);

    // 1) 네이버 보강 대상 ISBN 조회
    List<String> selectTempIsbnForNaver(@Param("limit") int limit);

    // 2) 네이버 결과로 TEMP 업데이트
    int updateTempFromNaver(BookIsbnTempDTO dto);

    // 3) 알라딘 보강 대상 ISBN 조회
    List<String> selectTempIsbnForAladin(@Param("limit") int limit);

    // 4) 알라딘 결과로 TEMP 업데이트
    int updateTempFromAladin(BookIsbnTempDTO dto);

    // 5) DATA_HASH 내용 TEMP 업데이트
    int updateTempDataHash(@Param("isbn13") String isbn13,
                           @Param("dataHash") String dataHash);

    // 6) READY
    int markTempReady(@Param("isbn13") String isbn13);


    // 7) MERGED(=2) 처리
    int markTempMerged(@Param("isbn13") String isbn13);

    // 8) TEMP 단건 조회 (READY 판정용)
    BookIsbnTempDTO selectTempByIsbn13(@Param("isbn13") String isbn13);

    // 9) READY(1) -> 운영 반영 대상 ISBN 조회
    List<String> selectTempIsbnForMerge(@Param("limit") int limit);

    // 10) TEMP(READY) -> BOOK_ISBN 업서트
    int upsertBookIsbnFromTemp(@Param("isbn13") String isbn13);

}
