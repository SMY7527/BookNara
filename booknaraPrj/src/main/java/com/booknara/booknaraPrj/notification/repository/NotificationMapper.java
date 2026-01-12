package com.booknara.booknaraPrj.notification.repository;

import com.booknara.booknaraPrj.notification.dto.NotificationDTO;
import com.booknara.booknaraPrj.notification.dto.NotificationQueryParam;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NotificationMapper {
    private final SqlSession session;

    // 새로운 미읽음 알림 갯수
    public int getUnread(String userId) {
        return session.selectOne("notification.countUnread", userId);
    }

    // 선택한 탭 알림 갯수 가져오기
    public int getNotiCnt(Map<String, Object> map) {
        return session.selectOne("notification.countNotification", map);
    }

    // 선택한 탭 별로 알림 가져오기
    public List<NotificationDTO> getNotifications(NotificationQueryParam nqp) {
        return session.selectList("notification.selectNotifications", nqp);
    }
}
