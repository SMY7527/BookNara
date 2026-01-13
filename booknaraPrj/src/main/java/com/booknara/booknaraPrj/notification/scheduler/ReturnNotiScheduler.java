package com.booknara.booknaraPrj.notification.scheduler;

import com.booknara.booknaraPrj.notification.dto.NotificationEntity;
import com.booknara.booknaraPrj.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;

@Component
@EnableScheduling
public class ReturnNotiScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationService notificationService;

    // ✅ 하루 3번: 09:00 / 13:00 / 18:00 (Asia/Seoul)
    @Scheduled(cron = "0 0 9,13,18 * * *", zone = "Asia/Seoul")
    public void sendReturnNotifications() {

        // 1) 반납 3일 전
        sendNotiBatch(
                "RETURN_D3",
                """
                SELECT l.LEND_ID, l.USER_ID, l.BOOK_ID, l.RETURN_DUE_DATE
                FROM LENDS l
                WHERE l.RETURN_DONE_AT IS NULL
                  AND DATE(l.RETURN_DUE_DATE) = DATE(DATE_ADD(NOW(), INTERVAL 3 DAY))
                  AND NOT EXISTS (
                      SELECT 1 FROM NOTIFICATION n
                      WHERE n.TARGET_TYPE = 'RETURN_D3'
                        AND n.TARGET_ID = l.LEND_ID
                        AND n.USER_ID = l.USER_ID
                  )
                """,
                (row) -> "반납 만료 3일 전입니다. 기한 내 반납해 주세요."
        );

        // 2) 반납 당일
        sendNotiBatch(
                "RETURN_TODAY",
                """
                SELECT l.LEND_ID, l.USER_ID, l.BOOK_ID, l.RETURN_DUE_DATE
                FROM LENDS l
                WHERE l.RETURN_DONE_AT IS NULL
                  AND DATE(l.RETURN_DUE_DATE) = CURDATE()
                  AND NOT EXISTS (
                      SELECT 1 FROM NOTIFICATION n
                      WHERE n.TARGET_TYPE = 'RETURN_TODAY'
                        AND n.TARGET_ID = l.LEND_ID
                        AND n.USER_ID = l.USER_ID
                  )
                """,
                (row) -> "오늘이 반납 만료일입니다. 오늘 안에 반납해 주세요."
        );

        // 3) 연체 (1회만)
        sendNotiBatch(
                "OVERDUE",
                """
                SELECT l.LEND_ID, l.USER_ID, l.BOOK_ID, l.RETURN_DUE_DATE
                FROM LENDS l
                WHERE l.RETURN_DONE_AT IS NULL
                  AND DATE(l.RETURN_DUE_DATE) < CURDATE()
                  AND NOT EXISTS (
                      SELECT 1 FROM NOTIFICATION n
                      WHERE n.TARGET_TYPE = 'OVERDUE'
                        AND n.TARGET_ID = l.LEND_ID
                        AND n.USER_ID = l.USER_ID
                  )
                """,
                (row) -> "반납 기한이 지나 연체되었습니다. 빠른 반납을 부탁드립니다."
        );
    }

    // =========================
    // 내부 유틸
    // =========================

    @FunctionalInterface
    interface MessageMaker {
        String make(Row row);
    }

    static class Row {
        final String lendId;
        final String userId;
        final long bookId;

        Row(String lendId, String userId, long bookId) {
            this.lendId = lendId;
            this.userId = userId;
            this.bookId = bookId;
        }
    }

    private void sendNotiBatch(String targetType, String sql, MessageMaker messageMaker) {
        List<Row> rows = jdbcTemplate.query(sql, (ResultSet rs, int idx) ->
                new Row(
                        rs.getString("LEND_ID"),
                        rs.getString("USER_ID"),
                        rs.getLong("BOOK_ID")
                )
        );

        for (Row row : rows) {
            NotificationEntity noti = new NotificationEntity();
            noti.setUserId(row.userId);
            noti.setTargetType(targetType);
            noti.setTargetId(row.lendId);
            noti.setNotiContent(messageMaker.make(row));

            // ✅ 알림 저장/발송은 서비스에서 처리 (DB 저장 포함)
            notificationService.saveNotification(noti);
        }
    }
}
