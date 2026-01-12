package com.booknara.booknaraPrj.login_signup.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequest {

    private String userId;
    private String password;

    private String userNm;       // 이름
    private String profileNm;    // 프로필 이름

    private String gender;       // M / F
    private LocalDate birthday;

    private String phoneNo;
    private String email;
    private String smsYn;        // Y / N

    private String addr;
    private String detailAddr;
    private String zipcode;
}
