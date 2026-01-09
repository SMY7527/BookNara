package com.booknara.booknaraPrj.admin.inquiry;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    public Inquiry getInquiry(String inqId) {
        return inquiryRepository.findById(inqId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. ID: " + inqId));
    }

    @Transactional
    public void save(Inquiry inquiry) {
        inquiryRepository.save(inquiry);
    }

    public Map<String, Long> getInquiryStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("pendingCount", inquiryRepository.countByRespState("N"));
        stats.put("resolvedCount", inquiryRepository.countByRespState("Y"));

        // "5"를 문자열로 전달 (엔티티의 inqType이 String이므로)
        stats.put("urgentCount", inquiryRepository.countByInqTypeAndRespState("5", "N"));

        return stats;
    }

    public List<Inquiry> getFilteredInquiries(String filter) {
        if ("pending".equals(filter)) {
            return inquiryRepository.findByRespState("N");
        } else if ("resolved".equals(filter)) {
            return inquiryRepository.findByRespState("Y");
        }
        return inquiryRepository.findAll(); // 기본값: 전체 조회
    }
}