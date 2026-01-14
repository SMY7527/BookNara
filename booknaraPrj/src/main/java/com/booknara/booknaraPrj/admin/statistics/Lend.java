package com.booknara.booknaraPrj.admin.statistics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "LENDS", schema = "BOOKNARA")
public class Lend {

    @Id
    @Column(name = "LEND_ID", length = 30)
    private String lendId; // LEND_0001 형식

    @Column(name = "BOOK_ID", nullable = false)
    private Long bookId;

    @Column(name = "USER_ID", length = 50, nullable = false)
    private String userId;

    @Column(name = "ISBN13", length = 20)
    private String isbn13; // 누락되었던 컬럼 추가

    @Column(name = "BOX_ID")
    private Long boxId; // DB 타입 BIGINT(20)에 맞춰 Long으로 변경

    @Column(name = "OVER_DUE", length = 1)
    private String overDue; // Y/N (CHAR 1)

    @Column(name = "LEND_DATE")
    private LocalDateTime lendDate;

    @Column(name = "RETURN_DUE_DATE")
    private LocalDateTime returnDueDate;

    @Column(name = "RETURN_BOX_AT")
    private LocalDateTime returnBoxAt;

    @Column(name = "RETURN_DONE_AT")
    private LocalDateTime returnDoneAt;

    @Column(name = "EXTEND_CNT")
    private Integer extendCnt; // TINYINT(4)

    @Column(name = "DELIVERY_STATUS", length = 20)
    private String deliveryStatus;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt; // 누락되었던 컬럼 추가

    @Column(name = "UPDATED_at") // DB 대소문자 표기 반영
    private LocalDateTime updatedAt; // 누락되었던 컬럼 추가

    @Column(name = "ACTIVE_FLAG")
    private Integer activeFlag; // 누락되었던 컬럼 추가 (TINYINT 4)
}