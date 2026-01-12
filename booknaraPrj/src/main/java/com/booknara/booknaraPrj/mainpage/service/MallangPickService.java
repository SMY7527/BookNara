package com.booknara.booknaraPrj.mainpage.service;

import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import com.booknara.booknaraPrj.mainpage.mapper.MallangPickMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MallangPickService {

    private final MallangPickMapper mallangPickMapper;

    public List<MallangPickDTO> findMallangPickBooks(int genreId) {
        return mallangPickMapper.selectMallangPickBooks(genreId);
    }
}
