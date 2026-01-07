package com.booknara.booknaraPrj.bookcirculation.service;

import com.booknara.booknaraPrj.bookcirculation.dto.BookCirculationStatusDTO;
import com.booknara.booknaraPrj.bookcirculation.mapper.BookCirculationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookCirculationService {

    private final BookCirculationMapper mapper;

    public BookCirculationStatusDTO getStatus(String isbn13, String userId) {
        return mapper.getStatus(isbn13, userId);
    }
}
