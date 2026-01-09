package com.booknara.booknaraPrj.mainpage.service;

import com.booknara.booknaraPrj.mainpage.dto.LibrarianBookDTO;
import com.booknara.booknaraPrj.mainpage.mapper.LibrarianBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibrarianBookService {

    private final LibrarianBookMapper librarianBookMapper;

    public List<LibrarianBookDTO> findLibrarianBooks() {
        return librarianBookMapper.selectLibrarianBooks();
    }
}

