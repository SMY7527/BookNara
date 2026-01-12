package com.booknara.booknaraPrj.mypage.info;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MyInfoDto {

    private String userId;      // 아이디
    private String userNm;      // 이름
    private String profileNm;   // 프로필명
    private LocalDate birthday; // 생년월일
    private String gender;      // 성별
    private String addr;     // 배송지
}
