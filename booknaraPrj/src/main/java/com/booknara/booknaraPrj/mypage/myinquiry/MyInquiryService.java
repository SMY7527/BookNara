package com.booknara.booknaraPrj.mypage.myinquiry;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyInquiryService {

    private final MyInquiryMapper inquiryMapper;

    public List<MyInquiryHistoryDto> getMyInquiry(String userId, String keyword) {
        return inquiryMapper.selectMyInquiry(userId, keyword);
    }

    public void writeInquiry(MyInquiryWriteDto dto) {
        inquiryMapper.insertInquiry(dto);
    }
}
