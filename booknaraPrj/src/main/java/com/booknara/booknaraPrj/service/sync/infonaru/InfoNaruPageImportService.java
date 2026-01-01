package com.booknara.booknaraPrj.service.sync.infonaru;

import com.booknara.booknaraPrj.client.infoNaru.InfoNaruClient;
import com.booknara.booknaraPrj.client.infoNaru.InfoNaruPageResult;
import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.mapper.BookBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfoNaruPageImportService {

    // 정보나루 API 호출 클라이언트
    private final InfoNaruClient infoNaruClient;

    // 도서 테이블 MyBatis Mapper
    private final BookBatchMapper bookBatchMapper;

    // 정보나루 인증키
    @Value("${api.infonaru.key}")
    private String infonaruKey;

    // 정보나루 API 페이지 1건을 DB에 적재
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean importOnePage(int pageNo, int pageSize) {

        // API 요청 파라미터 구성
        Map<String, String> params = new HashMap<>();
        params.put("authKey", infonaruKey);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("format", "json");

        // API 호출
        InfoNaruPageResult result = infoNaruClient.getBookPage(params);
        List<BookIsbnTempDTO> books = (result == null) ? List.of() : result.getBooks();

        // 응답이 없으면 종료
        if (books == null || books.isEmpty()) {
            log.warn("pageNo={} returned=0", pageNo);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        //null값 검증
        books = books.stream()
                .filter(b -> b.getIsbn13() != null && !b.getIsbn13().isBlank())
                .filter(b -> b.getBookTitle() != null && !b.getBookTitle().isBlank())
                .filter(b -> b.getPublisher() != null && !b.getPublisher().isBlank())
                .toList();

        books.forEach(book -> {
            book.setInfonaruFetchedAt(now);
            book.setStatusCd(0); // NOTREADY
        });



        // DB 부하 방지를 위해 1000건씩 분할 삽입
        int chunkSize = 1000;
        int inserted = 0;

        for (int i = 0; i < books.size(); i += chunkSize) {
            List<BookIsbnTempDTO> chunk =
                    books.subList(i, Math.min(i + chunkSize, books.size()));
            inserted += bookBatchMapper.insertBookIsbnTemp(chunk);
        }

        // 중복 ISBN으로 인해 삽입되지 않은 건수
        int returned = books.size();
        int skipped = returned - inserted;

        // 페이지별 처리 결과 로그
        log.info(
                "pageNo={}, requested={}, returned={}, inserted={}, skipped={}, numFound={}",
                pageNo, pageSize, returned, inserted, skipped, result.getNumFound()
        );

        return true;
    }
}

