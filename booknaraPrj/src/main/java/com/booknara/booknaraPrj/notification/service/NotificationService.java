package com.booknara.booknaraPrj.notification.service;

import com.booknara.booknaraPrj.notification.domain.NotiTab;
import com.booknara.booknaraPrj.notification.dto.NotificationDTO;
import com.booknara.booknaraPrj.notification.dto.NotificationEntity;
import com.booknara.booknaraPrj.notification.dto.NotificationQueryParam;
import com.booknara.booknaraPrj.notification.repository.NotificationMapper;
import com.booknara.booknaraPrj.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper mapper;
    private final NotificationRepository repo;

    // 새로운 알림 카운트
    public int findNewNotification(String userId) {
        return mapper.getUnread(userId);
    }

    // 알림 발생
    public void saveNotification(NotificationEntity notE) {
        repo.save(notE);
    }

    // 알림 단건 읽음 처리
    @Transactional
    public void notificationRead(String userId, long notiId) {
        NotificationEntity noti = repo.findByNotiIdAndUserId(notiId, userId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 없거나 권한이 없습니다."));

        if (noti.isUnread()) {
            noti.markRead();
        }
    }

    // 알림 전체 읽음 처리
    @Transactional
    public int notificationReadAll(String userId, String tab) {
        // 1) 탭별 targetType 목록 만들기
        NotiTab tabParam = NotiTab.valueOf(tab.toUpperCase());
        List<String> targetTypes = tabParam.getTargetTypes();

        // 2) 대상 엔티티 조회 (읽지 않은 것만)
        List<NotificationEntity> targets;
        if (tabParam == NotiTab.ALL || tabParam == NotiTab.UNREAD) {
            targets = repo.findByUserIdAndCheckYn(userId, 'N');
        } else {
            targets = repo.findByUserIdAndCheckYnAndTargetTypeIn(userId, 'N', targetTypes);
        }

        // 3) 변경 감지 업데이트
        for (NotificationEntity n : targets) {
            n.markRead();
        }

        // update된 target 개수 리턴
        return targets.size();
    }

    public int notiCnt(String userId, String tab, Character checkYn) {
        NotiTab tabParam = NotiTab.valueOf(tab.toUpperCase());
        List<String> targetTypes = tabParam.getTargetTypes();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("targetTypes", targetTypes);
        map.put("checkYn", checkYn);

        return mapper.getNotiCnt(map);
    }

    // 선택한 탭 별로 알림 가져오기
    public List<NotificationDTO> findNotifications(NotificationQueryParam nqp, String userId, String tab, int page, int size) {
        NotiTab tabParam = NotiTab.valueOf(tab.toUpperCase());
        List<String> targetTypes = tabParam.getTargetTypes();

        nqp.setUserId(userId);
        nqp.setTargetTypes(targetTypes);
        nqp.setLimit(size);
        nqp.setOffset(page * size);

        return mapper.getNotifications(nqp);
    }
}
