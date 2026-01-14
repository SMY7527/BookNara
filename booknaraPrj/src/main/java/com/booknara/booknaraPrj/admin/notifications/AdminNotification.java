package com.booknara.booknaraPrj.admin.notifications;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "ADMIN_NOTIFICATION", indexes = {
        @Index(name = "IDX_ADMIN_NOTI_CHECK_UPDATE", columnList = "CHECK_YN, UPDATED_AT"),
        @Index(name = "IDX_ADMIN_NOTI_CREATED", columnList = "CREATED_AT")
})
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTI_ID")
    private Long notiId;

    @Column(name = "USER_ID", length = 50, nullable = false)
    private String userId;

    @Column(name = "USER_NM", length = 50, nullable = false)
    private String userNm;

    @Column(name = "LEND_ID", length = 30, nullable = false)
    private String lendId;

    @Column(name = "NOTI_TYPE", length = 20, nullable = false)
    private String notiType; // 대여, 반납, 연체

    @Column(name = "NOTI_CONTENT", length = 500, nullable = false)
    private String notiContent;

    @Column(name = "CHECK_YN", length = 1, nullable = false)
    @ColumnDefault("'N'")
    private String checkYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}