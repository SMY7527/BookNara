package com.booknara.booknaraPrj.admin.statistics;

public interface UserAgeStatProjection {
    String getLabel();    // 연령대 (예: 20대)
    Long getCount();      // 해당 인원수
    Double getPercentage(); // 비율
}
