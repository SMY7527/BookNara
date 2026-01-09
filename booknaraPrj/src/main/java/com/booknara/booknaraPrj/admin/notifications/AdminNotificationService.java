package com.booknara.booknaraPrj.admin.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;

    /**
     * 통합 알림 목록 조회 (필터링 + 검색 + 페이징)
     * 도서 정보와 사용자 정보를 포함한 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Page<AdminNotiResponseDto> getDetailedNotifications(String type, String keyword, Pageable pageable) {
        // '전체'일 경우 null을 전달하여 레포지토리 쿼리에서 모든 타입을 조회하도록 함
        String typeFilter = (type == null || "전체".equals(type)) ? null : type;

        // 검색어가 공백일 경우 null 처리 (쿼리 최적화)
        String keywordFilter = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        return adminNotificationRepository.findDetailedNotifications(typeFilter, keywordFilter, pageable);
    }

    /**
     * 알림 읽음 처리 (상태 변경 및 확인 시점 기록)
     */
    @Transactional
    public void markAsRead(Long notiId) {
        AdminNotification noti = adminNotificationRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다. id=" + notiId));

        noti.setCheckYn("Y");
        noti.setUpdatedAt(LocalDateTime.now());
        // @Transactional에 의해 메서드 종료 시 Dirty Checking으로 DB에 자동 반영됩니다.
    }

    /**
     * 사이드바 배지용 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return adminNotificationRepository.countByCheckYn("N");
    }

    @Transactional
    public void markAllAsRead() {
        adminNotificationRepository.markAllAsRead(LocalDateTime.now());
    }
}