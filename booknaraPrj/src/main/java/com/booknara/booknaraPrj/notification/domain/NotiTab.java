package com.booknara.booknaraPrj.notification.domain;

import java.util.List;

public enum NotiTab {
    ALL(null),        // 타입 필터 없음
    UNREAD(null),     // 타입 필터 없음 + checkYn = N
    SYSTEM(List.of("RENTAL_DUE", "RESERVATION_AVAILABLE", "ACCOUNT_RESTRICTED", "PAST_DUE")),
    DELIVERY(List.of("DELIVERY_START", "DELIVERY_ARRIVE")),
    COMMUNITY(List.of("FEED_COMMENT", "FEED_LIKE")),
    EVENT(List.of("EVENT_WON")),
    ADMIN(List.of("ADMIN_MESSAGE", "INQUIRY_ANSWERED"));

    private final List<String> targetTypes;

    NotiTab(List<String> targetTypes) {
        this.targetTypes = targetTypes;
    }

    public List<String> getTargetTypes() {
        return targetTypes;
    }
}
