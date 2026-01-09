package com.booknara.booknaraPrj.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    long notiId;
    String targetType;
    String targetId;
    String notiContent;
    LocalDateTime createdAt;
    char checkYn;
}
