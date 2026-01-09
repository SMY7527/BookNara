package com.booknara.booknaraPrj.admin.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {
    // 최신순으로 전체 문의 목록 가져오기
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // 1. 답변 대기 건수 (respState = 'N')
    long countByRespState(String respState);

    // 2. 긴급/신고 건수 (inqType = 5(신고) 이면서 답변 대기인 경우)
    long countByInqTypeAndRespState(String inqType, String respState);

    // 상태에 따른 목록 조회 (Pending, Resolved용)
    List<Inquiry> findByRespState(String respState);

    // 신고 유형이면서 답변 대기인 목록 조회 (Urgent용)
    List<Inquiry> findByInqTypeAndRespState(String inqType, String respState);
}
