package com.booknara.booknaraPrj.notification.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Data
@NoArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long notiId;

    @Column(name = "USER_ID", nullable = false, length = 50)
    String userId;

    @Column(name = "TARGET_TYPE", nullable = false, length = 40)
    String targetType;

    @Column(name = "TARGET_ID", length = 50)
    String targetId;

    @Column(name = "NOTI_CONTENT", nullable = false, length = 500)
    String notiContent;

//    @Column(name = "CREATED_AT", nullable = false)
//    private LocalDateTime createdAt;

    @Column(name = "CHECK_YN", nullable = false, length = 1)
    private char checkYn; // 'N' or 'Y'

    public boolean isUnread() { return checkYn == 'N'; }

    public void markRead() { this.checkYn = 'Y'; }
}
