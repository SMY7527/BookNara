package com.booknara.booknaraPrj.login_signup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraAddressRequest {
    private String zipcode;
    private String addr;
    private String detailAddr;
}
