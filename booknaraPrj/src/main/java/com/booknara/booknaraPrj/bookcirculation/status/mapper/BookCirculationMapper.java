package com.booknara.booknaraPrj.bookcirculation.status.mapper;

import com.booknara.booknaraPrj.bookcirculation.status.dto.BookCirculationStatusDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * [BookCirculationMapper]
 * 도서의 가용성(재고, 대출 중, 예약 중)과 로그인 사용자의 개인화 상태를
 * 실시간으로 조회하기 위한 데이터 접근 인터페이스입니다.
 */
@Mapper
public interface BookCirculationMapper {

    /**
     * [도서 순환 상태 통합 조회]
     * - 특정 도서(ISBN)에 대한 모든 상태 정보를 집계하여 반환합니다.
     * - SQL 내부에서는 조인과 집계 함수(COUNT)를 사용하여 재고 현황을 산출하고,
     * userId가 존재할 경우 해당 사용자의 현재 이용 상태(대출 여부, 예약 여부 등)를 매핑합니다.
     * * @param isbn13 조회할 도서의 고유 번호
     * @param userId 현재 접속 중인 사용자 ID (비로그인 시 null 전달 가능)
     * @return 재고 및 개인화 상태 정보가 포함된 DTO
     */
    BookCirculationStatusDTO getStatus(@Param("isbn13") String isbn13,
                                       @Param("userId") String userId);
}