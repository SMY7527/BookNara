package com.booknara.booknaraPrj.ebook.repository;

import com.booknara.booknaraPrj.ebook.dto.MyEBookItemDTO;
import com.booknara.booknaraPrj.ebook.dto.SaveCfiRequest;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EBookRepository {
    private final SqlSession session;

    // ISBN13으로 도서 epub 검색
    public String getEpubByISBN13(String isbn) {
        return session.selectOne("ebook.selectEpubByISBN", isbn);
    }

    // 도서 번호로 도서 epub 검색
    public String getEpubByBookID(long id) {
        return session.selectOne("ebook.selectEpubByBook", id);
    }

    // 도서 번호로 도서 isbn 검색
    public String getIsbnByBookID(long id) {
        return session.selectOne("ebook.selectISBNByBook", id);
    }

    //
    public SaveCfiRequest getCfi(Map<String, String> map) {
        return session.selectOne("ebook.selectCfi", map);
    }

    // 도서 검증 ID
    public boolean existsActiveLendByBook(String userId, Long bookId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("bookId", bookId);

        int result = session.selectOne("ebook.selectUserBook", map);
        return result == 1;
    }

    // 도서 검증 ISBN
    public boolean existsActiveLendByIsbn(String userId, String isbn) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("isbn", isbn);
        int result = session.selectOne("ebook.selectUserIsbn", map);
        return result == 1;
    }

    // 회원의 대여중인 전자책 목록 검색
    public List<MyEBookItemDTO> getEBookList(String user) {
        return session.selectList("ebook.selectEBookList", user);
    }

    // cfi 저장
    public void setCfiToHistory(Map<String, String> map) {
        session.insert("ebook.saveCfi", map);
    }
}
