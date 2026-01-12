package com.booknara.booknaraPrj.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationQueryParam {
    private String userId;
    private char checkYn;
    private List<String> targetTypes;
    private int limit;
    private int offset;
}
