package com.booknara.booknaraPrj.admin.notifications;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ADMIN_NOTIFICATION")
@Getter
@Setter
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notiId;

    private String userId;
    private String userNm;
    private String lendId;
    private String notiType;
    private String notiContent;

    @Column(columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String checkYn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
