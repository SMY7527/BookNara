package com.booknara.booknaraPrj.mypage.mylibrary;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper {

    void insertPayment(PaymentDto dto);
}
