package com.booknara.booknaraPrj.admin.inquiry;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminInquiryService {
    private final AdminInquiryRepository adminInquiryRepository;


    public AdminInquiry getInquiry(String inqId) {
        return adminInquiryRepository.findById(inqId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. ID: " + inqId));
    }

    @Transactional
    public void save(AdminInquiry adminInquiry) {
        adminInquiryRepository.save(adminInquiry);
    }

    public long getTotalCount() { return adminInquiryRepository.count(); }
    public long getPendingCount() { return adminInquiryRepository.countByRespState("N"); }
    public long getResolvedCount() { return adminInquiryRepository.countByRespState("Y"); }

    // 페이징 처리 메소드 (예시)
    public Page<AdminInquiry> getInquiries(String type, String status, Pageable pageable) {
        // 1. 유형 필터: REPORT가 선택되었다면 문의 내역은 보여줄 필요 없음 (빈 페이지 반환)
        if ("REPORT".equals(type)) return Page.empty(pageable);

        // 2. 상태 필터 변환: PENDING -> N, RESOLVED -> Y
        String dbStatus = null;
        if ("PENDING".equals(status)) dbStatus = "N";
        else if ("RESOLVED".equals(status)) dbStatus = "Y";

        // 3. 필터에 따른 조회
        if (dbStatus == null) return adminInquiryRepository.findAll(pageable); // ALL인 경우
        return adminInquiryRepository.findByRespState(dbStatus, pageable);
    }
}