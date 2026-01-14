package com.booknara.booknaraPrj.bookcart.mapper;

import com.booknara.booknaraPrj.bookcart.dto.BookCartDTO;
import com.booknara.booknaraPrj.bookcart.dto.UserAddressDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * [BookCartMapper]
 * 사용자의 장바구니 관리, 대여 한도 조회, 배송지 관리 등
 * 대여 전 단계의 모든 DB 프로세스를 담당하는 핵심 매퍼입니다.
 */
@Mapper
public interface BookCartMapper {

    /** [C] 장바구니 추가: 특정 유저의 장바구니에 도서(ISBN)를 담습니다. */
    int insert(@Param("userId") String userId,
               @Param("isbn13") String isbn13);

    /** [D] 단일 삭제: 특정 유저의 장바구니에서 특정 항목을 제거합니다. */
    int delete(@Param("userId") String userId,
               @Param("cartId") Long cartId);

    /** [D] 전체 삭제: 유저가 대여를 완료하거나 수동으로 장바구니를 비울 때 사용합니다. */
    int deleteAll(@Param("userId") String userId);

    /** [D] ISBN 기반 삭제: 도서 상세 등에서 장바구니 해제 시 사용합니다. */
    int deleteByIsbn(@Param("userId") String userId,
                     @Param("isbn13") String isbn13);

    /** [R] 목록 조회: 장바구니에 담긴 도서 정보와 실시간 대여 가능 여부를 함께 가져옵니다. */
    List<BookCartDTO> selectList(@Param("userId") String userId);

    /** [STAT] 내 장바구니 수: 현재 담긴 도서 수를 집계합니다. */
    int countMyCart(@Param("userId") String userId);

    /** [STAT] 현재 대여중 수: 사용자가 반납하지 않은 도서 수를 집계합니다. (한도 계산용) */
    int countMyActiveLends(@Param("userId") String userId);

    /** [SETTING] 시스템 정책 조회: 도서관 설정 테이블에서 최대 대여 가능 권수를 가져옵니다. */
    Integer selectMaxLendCount();

    /** [R] 존재 여부: 이미 장바구니에 담긴 책인지 확인하여 중복 담기를 방지합니다. */
    int existsByIsbn(@Param("userId") String userId,
                     @Param("isbn13") String isbn13);

    /** [R] 기본 주소 조회: 대여 시 배송/방문 정보를 위해 유저의 기본 주소를 가져옵니다. */
    UserAddressDTO selectMyDefaultAddress(@Param("userId") String userId);

    /** [U] 주소 업데이트: 대여 프로세스 중 주소를 변경할 경우 정보를 갱신합니다. */
    int updateMyDefaultAddress(UserAddressDTO dto);

    /** [R] 실시간 재고 확인: 특정 ISBN의 책이 현재 관내에 대여 가능한 상태인지 최종 확인합니다. */
    Boolean isLendableByIsbn(@Param("isbn13") String isbn13);

    /** [C] 배송 연동: 대여 완료 시 배송(Delivery) 테이블에 초기 데이터를 생성합니다. */
    void insertDelivery(String lendId);

}