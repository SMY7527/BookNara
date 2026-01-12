package com.booknara.booknaraPrj.bookcart.mapper;

import com.booknara.booknaraPrj.bookcart.dto.BookCartDTO;
import com.booknara.booknaraPrj.bookcart.dto.UserAddressDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookCartMapper {

    int insert(@Param("userId") String userId,
               @Param("isbn13") String isbn13);

    int delete(@Param("userId") String userId,
               @Param("cartId") Long cartId);

    int deleteAll(@Param("userId") String userId);

    int deleteByIsbn(@Param("userId") String userId,
                     @Param("isbn13") String isbn13);

    List<BookCartDTO> selectList(@Param("userId") String userId);

    // 장바구니 담긴 권수
    int countMyCart(@Param("userId") String userId);

    // 현재 대여중 권수
    int countMyActiveLends(@Param("userId") String userId);

    // 최대 대여 가능 권수 (SETTINGS)
    Integer selectMaxLendCount();

    //장바구니 담김 여부
    int existsByIsbn(@Param("userId") String userId,
                     @Param("isbn13") String isbn13);


    //유저 주소 삽입
    UserAddressDTO selectMyDefaultAddress(@Param("userId") String userId);
    int updateMyDefaultAddress(UserAddressDTO dto);


}
