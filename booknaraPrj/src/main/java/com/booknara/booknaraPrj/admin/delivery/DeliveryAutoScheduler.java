package com.booknara.booknaraPrj.admin.delivery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class DeliveryAutoScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1분마다 체크하여 단계별 자동 업데이트
    @Scheduled(fixedDelay = 60000)
    public void autoFillDeliverySteps() {

        // 1단계: 생성 후 10분 뒤 도서 준비 완료
        jdbcTemplate.update("UPDATE DELIVERY_H SET BOOK_READY_AT = NOW() " +
                "WHERE BOOK_READY_AT IS NULL AND CREATED_AT <= NOW() - INTERVAL 10 MINUTE");

        // 2단계: 준비 완료 30분 뒤 픽업 완료
        jdbcTemplate.update("UPDATE DELIVERY_H SET PICKUP_AT = NOW() " +
                "WHERE BOOK_READY_AT IS NOT NULL AND PICKUP_AT IS NULL " +
                "AND BOOK_READY_AT <= NOW() - INTERVAL 30 MINUTE");

        // 3단계: 픽업 1시간 뒤 허브 도착
        jdbcTemplate.update("UPDATE DELIVERY_H SET HUB_AT = NOW() " +
                "WHERE PICKUP_AT IS NOT NULL AND HUB_AT IS NULL " +
                "AND PICKUP_AT <= NOW() - INTERVAL 1 HOUR");

        // 4단계: 허브 도착 2시간 뒤 배송 시작
        jdbcTemplate.update("UPDATE DELIVERY_H SET DLV_START_AT = NOW() " +
                "WHERE HUB_AT IS NOT NULL AND DLV_START_AT IS NULL " +
                "AND HUB_AT <= NOW() - INTERVAL 2 HOUR");

        // 5단계: 배송 시작 3시간 뒤 배송 완료
        jdbcTemplate.update("UPDATE DELIVERY_H SET DLV_END_AT = NOW() " +
                "WHERE DLV_START_AT IS NOT NULL AND DLV_END_AT IS NULL " +
                "AND DLV_START_AT <= NOW() - INTERVAL 3 HOUR");
    }
}