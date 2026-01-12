package com.booknara.booknaraPrj.admin.Event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {

    private final AdminEventsRepository adminEventsRepository;

    /**
     * [수정] 종료되지 않은 전체 이벤트를 리스트로 조회 (페이징 제거)
     * end_at이 현재 시간보다 큰 경우만 포함
     */
    public List<AdminEvent> getAllActiveEvents() {
        return adminEventsRepository.findAllByEndAtAfter(LocalDateTime.now());
    }

    /**
     * 현재 메인 배너로 활성화된 이벤트만 조회
     */
    public List<AdminEvent> getActiveBanners() {
        return adminEventsRepository.findByEventMainYn("Y");
    }

    /**
     * 메인 배너 일괄 업데이트 로직
     * @param selectedIds 선택된 이벤트 ID 리스트
     */
    @Transactional
    public void updateMainBanners(List<Long> selectedIds) {
        // 1. 기존 모든 배너의 활성화 상태를 'N'으로 초기화
        adminEventsRepository.resetAllMainBanner();

        // 2. 선택된 ID가 있다면 해당 항목들만 'Y'로 업데이트
        if (selectedIds != null && !selectedIds.isEmpty()) {
            adminEventsRepository.updateMainBannerStatus(selectedIds);
        }
    }
}