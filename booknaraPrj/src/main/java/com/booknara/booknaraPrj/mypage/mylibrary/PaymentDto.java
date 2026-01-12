package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentDto {

    private Long paymentId;      // PK (AUTO_INCREMENT)
    private String userId;
    private String receiptId;    // null 가능
    private String status;       // PAID
    private LocalDateTime paidAt;
}
