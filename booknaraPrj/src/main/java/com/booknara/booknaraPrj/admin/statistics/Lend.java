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
@Table(name = "LENDS")
public class Lend {
    @Id
    private String lendId; // LEND_0001 형식

    private Long bookId;
    private String userId;
    private Integer boxId;

    @Column(length = 1)
    private String overDue; // Y/N

    private LocalDateTime lendDate;
    private LocalDateTime returnDueDate;
    private LocalDateTime returnBoxAt;
    private LocalDateTime returnDoneAt;

    private Integer extendCnt;
    private String deliveryStatus;
}
