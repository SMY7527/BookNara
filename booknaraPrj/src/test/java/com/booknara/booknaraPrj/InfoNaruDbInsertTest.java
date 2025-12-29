package com.booknara.booknaraPrj;

import com.booknara.booknaraPrj.client.infoNaru.InfoNaruClient;
import com.booknara.booknaraPrj.domain.BookDTO;
import com.booknara.booknaraPrj.mapper.BookMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InfoNaruDbInsertTest {

    @Autowired InfoNaruClient infoNaruClient;
    @Autowired BookMapper bookMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @Value("${api.infonaru.key}")
    String key;

    @Test
    void api_to_db_insert() {
        // 0) 지금 테스트가 붙은 DB(스키마) 확인
        String dbName = jdbcTemplate.queryForObject("select database()", String.class);
        System.out.println("CONNECTED DB = " + dbName);

        // 1) insert 전 테이블 건수
        Integer beforeCount = jdbcTemplate.queryForObject("select count(*) from book_isbn", Integer.class);
        System.out.println("book_isbn count(before) = " + beforeCount);
        assertNotNull(beforeCount);

        // 2) API 호출
        Map<String, String> params = new HashMap<>();
        params.put("authKey", key);
        params.put("pageNo", "1");
        params.put("pageSize", "10");
        params.put("format", "json");

        List<BookDTO> books = infoNaruClient.getBookList(params);
        assertNotNull(books, "API 결과 리스트가 null이면 안 됩니다.");
        assertFalse(books.isEmpty(), "API 결과가 비어있습니다. authKey/요청 파라미터를 확인하세요.");

        // 3) 샘플 데이터 출력(필수 필드 확인)
        BookDTO first = books.get(0);
        System.out.println("----- first book from API -----");
        System.out.println("isbn13   = " + first.getIsbn13());
        System.out.println("bookname = " + first.getBookname());
        System.out.println("authors  = " + first.getAuthors());
        System.out.println("publisher= " + first.getPublisher());
        System.out.println("--------------------------------");

        // isbn13은 PK라서 비면 insert가 의미가 없음
        assertNotNull(first.getIsbn13(), "isbn13이 null입니다. DTO 매핑 또는 API 응답을 확인하세요.");
        assertFalse(first.getIsbn13().isBlank(), "isbn13이 공백입니다. DTO 매핑 또는 API 응답을 확인하세요.");

        // 4) DB insert
        int inserted = bookMapper.insertBook(books);
        System.out.println("inserted = " + inserted);

        // INSERT IGNORE 이므로 0도 정상(이미 존재하는 ISBN이면)
        assertTrue(inserted >= 0, "inserted는 음수가 될 수 없습니다.");

        // 5) insert 후 테이블 건수
        Integer afterCount = jdbcTemplate.queryForObject("select count(*) from book_isbn", Integer.class);
        System.out.println("book_isbn count(after) = " + afterCount);
        assertNotNull(afterCount);

        // 6) 샘플 ISBN이 DB에 존재하는지 확인 (가장 확실)
        Integer exists = jdbcTemplate.queryForObject(
                "select count(*) from book_isbn where isbn13 = ?",
                Integer.class,
                first.getIsbn13()
        );
        System.out.println("exists(first.isbn13) = " + exists);
        assertNotNull(exists);
        assertTrue(exists >= 1, "insert 후에도 샘플 ISBN이 DB에 없습니다. (DB 스키마/테이블/제약조건/IGNORE 동작 확인 필요)");
    }
}
