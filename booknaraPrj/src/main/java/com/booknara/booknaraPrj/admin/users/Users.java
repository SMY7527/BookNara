package com.booknara.booknaraPrj.admin.users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
public class Users {

    @Id
    @Column(name = "USER_ID", length = 50)
    private String userId;

    @Column(name = "USER_NM", nullable = false, length = 50)
    private String userNm;

    @Column(name = "PROFILE_NM", nullable = false, unique = true, length = 50)
    private String profileNm;

    @Column(name = "PROFILE_IMG", length = 2048)
    private String profileImg;

    @Column(name = "USE_IMG", nullable = false)
    private Integer useImg = 0; // 0=MALLANG, 1=PROFILE

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "GENDER", nullable = false, length = 1)
    private String gender; // M/F

    @Column(name = "BIRTHDAY", nullable = false)
    private LocalDate birthday;

    @Column(name = "ADDR", nullable = false, length = 255)
    private String addr;

    @Column(name = "DETAIL_ADDR", nullable = false, length = 255)
    private String detailAddr;

    @Column(name = "ZIPCODE", nullable = false, length = 20)
    private String zipcode;

    @Column(name = "PHONE_NO", nullable = false, length = 30)
    private String phoneNo; // PHONE_NM에서 PHONE_NO로 수정

    @Column(name = "EMAIL", nullable = false, length = 120)
    private String email;

    @Column(name = "SMS_YN", nullable = false, length = 1)
    private String smsYn = "N"; // Y/N

    @Column(name = "USER_ROLE", nullable = false)
    private Integer userRole = 1; // 0=ADMIN, 1=USER

    @Column(name = "USER_STATE", nullable = false, length = 1)
    private String userState = "1"; // 1=ACTIVE, 2=DORMANT, 3=BLOCKED, 4=WITHDRAWN

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    /**
     * 회원 상태 변경 메소드
     */
    public void updateState(String newState) {
        this.userState = newState;
    }
}