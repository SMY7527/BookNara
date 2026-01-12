package com.booknara.booknaraPrj.bookcart.dto;

import lombok.Data;

@Data
public class UserAddressDTO {
    private String userId;     // update에 필요
    private String zipcode;
    private String addr;
    private String detailAddr;
}
