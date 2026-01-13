package com.booknara.booknaraPrj.bookAPI.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class OverdueBanScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ 하루 3번: 09:00 / 13:00 / 18:00 (KST)
    @Scheduled(cron = "0 0 9,13,18 * * *", zone = "Asia/Seoul")
//    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void run() {

        // 0) (중요) 제재 카운트 중(END_AT 미래)인데 다시 연체가 생기면 카운트 중단
        pauseCountdownIfOverdueAgain();

        // 1) 신규 연체 유저: 제재 걸기 (USER_STATE=3, BAN insert END_AT=NULL)
        banNewOverdueUsers();

        // 2) 연체가 모두 해소된 유저: END_AT=NOW()+3일 (카운트 시작)
        startBanCountdownForClearedUsers();

        // 3) END_AT 지난 유저: 제재 해제 (USER_STATE=1)
        unbanExpiredUsers();
    }

    // =========================
    // 0) 제재 카운트 진행 중(END_AT > NOW())에 재연체 발생 시 END_AT=NULL로 되돌려 카운트 중단
    // =========================
    private void pauseCountdownIfOverdueAgain() {
        jdbcTemplate.update("""
            UPDATE BAN b
            SET b.END_AT = NULL,
                b.UPDATED_AT = NOW()
            WHERE b.END_AT IS NOT NULL
              AND b.END_AT > NOW()
              AND EXISTS (
                  SELECT 1
                  FROM LENDS l
                  WHERE l.USER_ID = b.USER_ID
                    AND l.RETURN_DONE_AT IS NULL
                    AND DATE(l.RETURN_DUE_DATE) < CURDATE()
              )
        """);
    }

    // =========================
    // 1) 신규 연체 발생: BAN 생성(END_AT=NULL) + USER_STATE=3
    // =========================
    private void banNewOverdueUsers() {
        // 활성 BAN이 없는 연체 유저만 BAN 생성
        jdbcTemplate.update("""
            INSERT INTO BAN (USER_ID, BAN_REASON, BAN_AT, END_AT)
            SELECT DISTINCT l.USER_ID,
                   'OVERDUE',
                   NOW(),
                   NULL
            FROM LENDS l
            WHERE l.RETURN_DONE_AT IS NULL
              AND DATE(l.RETURN_DUE_DATE) < CURDATE()
              AND NOT EXISTS (
                  SELECT 1
                  FROM BAN b
                  WHERE b.USER_ID = l.USER_ID
                    AND (b.END_AT IS NULL OR b.END_AT > NOW())
              )
        """);

        // 연체 중이면 USER_STATE=3
        jdbcTemplate.update("""
            UPDATE USERS u
            SET u.USER_STATE = 3
            WHERE u.USER_STATE <> 3
              AND EXISTS (
                  SELECT 1
                  FROM LENDS l
                  WHERE l.USER_ID = u.USER_ID
                    AND l.RETURN_DONE_AT IS NULL
                    AND DATE(l.RETURN_DUE_DATE) < CURDATE()
              )
        """);
    }

    // =========================
    // 2) 연체가 모두 해소된 경우: END_AT = NOW()+3일로 세팅 (제재 카운트 시작)
    // =========================
    private void startBanCountdownForClearedUsers() {
        jdbcTemplate.update("""
            UPDATE BAN b
            SET b.END_AT = DATE_ADD(NOW(), INTERVAL 3 DAY),
                b.UPDATED_AT = NOW()
            WHERE b.END_AT IS NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM LENDS l
                  WHERE l.USER_ID = b.USER_ID
                    AND l.RETURN_DONE_AT IS NULL
                    AND DATE(l.RETURN_DUE_DATE) < CURDATE()
              )
        """);
    }

    // =========================
    // 3) 제재 해제: END_AT 지난 유저 -> USER_STATE=1
    // =========================
    private void unbanExpiredUsers() {
        jdbcTemplate.update("""
            UPDATE USERS u
            SET u.USER_STATE = 1
            WHERE u.USER_STATE = 3
              AND NOT EXISTS (
                  -- 아직 연체중이면 해제 X
                  SELECT 1
                  FROM LENDS l
                  WHERE l.USER_ID = u.USER_ID
                    AND l.RETURN_DONE_AT IS NULL
                    AND DATE(l.RETURN_DUE_DATE) < CURDATE()
              )
              AND NOT EXISTS (
                  -- 활성 제재(END_AT NULL or >NOW)가 남아있으면 해제 X
                  SELECT 1
                  FROM BAN b2
                  WHERE b2.USER_ID = u.USER_ID
                    AND (b2.END_AT IS NULL OR b2.END_AT > NOW())
              )
              AND EXISTS (
                  -- 만료된 제재 기록이 있어야 해제
                  SELECT 1
                  FROM BAN b
                  WHERE b.USER_ID = u.USER_ID
                    AND b.END_AT IS NOT NULL
                    AND b.END_AT <= NOW()
              )
        """);
    }
}
