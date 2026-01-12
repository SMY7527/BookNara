package com.booknara.booknaraPrj.admin.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminInquiryRepository extends JpaRepository<AdminInquiry, String> {
    // 최신순으로 전체 문의 목록 가져오기
    List<AdminInquiry> findAllByOrderByCreatedAtDesc();

    // 1. 답변 대기 건수 (respState = 'N')
    long countByRespState(String respState);

    // InquiryRepository.java
    Page<AdminInquiry> findAll(Pageable pageable);
    Page<AdminInquiry> findByRespState(String respState, Pageable pageable);
}
