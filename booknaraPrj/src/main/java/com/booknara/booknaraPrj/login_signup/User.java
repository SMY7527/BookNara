package com.booknara.booknaraPrj.login_signup;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String userId;
    private String userNm;
    private String profileNm;
    private String profileImg;
    private int useImg;

    private String password;
    private String gender;
    private LocalDate birthday;

    private String addr;
    private String detailAddr;
    private String zipcode;

    private String extraInfoDone;

    private String phoneNo;
    private String email;
    private String smsYn="N";

    private int userRole;
    private String userState;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;



}

