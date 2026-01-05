package com.booknara.booknaraPrj.admin.inquiry;

import com.booknara.booknaraPrj.admin.users.Users;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "INQUIRY")
@Data
public class Inquiry {
    @Id
    private String inqId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private Users user; // 작성자 정보

    private String inqTitle;
    private String inqType;
    private String inqContent;
    private String respState; // 'Y' or 'N'
    private String respContent;
    private LocalDateTime respAt;
    private String RespUserId;
    private LocalDateTime createdAt;

    public String getInqTypeName() {
        if (this.inqType == null) return "기타";

        return switch (this.inqType) {
            case "1" -> "도서관리";
            case "2" -> "대여/반납";
            case "3" -> "시스템";
            case "4" -> "계정";
            case "5" -> "신고";
            default -> "기타";
        };
    }
}