package com.booknara.booknaraPrj.reviewstatus.service;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import com.booknara.booknaraPrj.reviewstatus.mapper.ReviewStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewStatusService {

    private final ReviewStatusMapper reviewStatusMapper;

    public ReviewStatusDTO getByIsbn(String isbn13) {
        if (isbn13 == null || isbn13.isBlank()) return null;
        return reviewStatusMapper.selectByIsbn(isbn13);
    }

    public List<ReviewStatusDTO> getByIsbns(List<String> isbns) {
        if (isbns == null || isbns.isEmpty()) return Collections.emptyList();
        return reviewStatusMapper.selectByIsbns(isbns);
    }
}
