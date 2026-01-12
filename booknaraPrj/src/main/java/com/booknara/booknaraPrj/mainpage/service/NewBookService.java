package com.booknara.booknaraPrj.mainpage.service;

import com.booknara.booknaraPrj.mainpage.dto.NewBookDTO;
import com.booknara.booknaraPrj.mainpage.mapper.NewBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewBookService {

    private final NewBookMapper newBookMapper;

    public List<NewBookDTO> findLatestBooks() {
        return newBookMapper.selectLatestBooks();
    }
}