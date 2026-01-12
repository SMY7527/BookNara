package com.booknara.booknaraPrj.admin.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCombinedSupportService {

    private final AdminCombinedSupportRepository adminCombinedSupportRepository;

    public Page<AdminCombinedSupport> getFilteredList(String keyword, String type, String status,
                                                      String sortField, String sortDir, Pageable pageable) {
        return adminCombinedSupportRepository.findFilteredList(keyword, type, status, sortField, sortDir, pageable);
    }

    // 카드에 표시할 대기/완료 개수 조회
    public long getCountByState(String state) {
        return adminCombinedSupportRepository.countByState(state);
    }
}