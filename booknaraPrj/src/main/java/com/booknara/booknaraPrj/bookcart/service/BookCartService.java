package com.booknara.booknaraPrj.bookcart.service;

import com.booknara.booknaraPrj.bookcart.dto.BookCartDTO;
import com.booknara.booknaraPrj.bookcart.mapper.BookCartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCartService {

    private final BookCartMapper mapper;

    public void add(String userId, String isbn13) {
        mapper.insert(userId, isbn13);
    }

    public void remove(String userId, Long cartId) {
        mapper.delete(userId, cartId);
    }

    public void clear(String userId) {
        mapper.deleteAll(userId);
    }

    public boolean toggle(String userId, String isbn13) {
        // 1) 있으면 삭제 시도
        int deleted = mapper.deleteByIsbn(userId, isbn13);
        if (deleted > 0) return false; // 이제 장바구니 아님

        // 2) 없으면 추가
        mapper.insert(userId, isbn13);
        return true; // 이제 장바구니 됨
    }


    public List<BookCartDTO> list(String userId) {
        return mapper.selectList(userId);
    }


}
