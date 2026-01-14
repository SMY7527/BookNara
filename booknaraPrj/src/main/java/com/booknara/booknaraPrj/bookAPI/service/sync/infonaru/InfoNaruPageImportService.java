package com.booknara.booknaraPrj.bookAPI.service.sync.infonaru;

import com.booknara.booknaraPrj.bookAPI.client.infoNaru.InfoNaruClient;
import com.booknara.booknaraPrj.bookAPI.client.infoNaru.InfoNaruPageResult;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.bookAPI.mapper.BookBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [InfoNaruPageImportService]
 * 정보나루 API를 호출하여 도서 목록을 페이지 단위로 DB(Staging 테이블)에 적재합니다.
 * 전체 수집 파이프라인의 1단계(Seed Data 생성)를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfoNaruPageImportService {

    // 정보나루 Open API 통신 클라이언트
    private final InfoNaruClient infoNaruClient;

    // 배치 처리용 MyBatis 매퍼
    private final BookBatchMapper bookBatchMapper;

    // application.yml 등에 설정된 API 인증키
    @Value("${api.infonaru.key}")
    private String infonaruKey;

    /**
     * 정보나루 API로부터 특정 페이지의 도서 목록을 가져와 DB에 삽입합니다.
     * @param pageNo 수집할 페이지 번호
     * @param pageSize 페이지당 도서 수
     * @return 성공 여부 (데이터 수신 여부 기준)
     */
    public boolean importOnePage(int pageNo, int pageSize) {

        // 1) API 요청 파라미터 구성 (JSON 포맷 강제)
        Map<String, String> params = new HashMap<>();
        params.put("authKey", infonaruKey);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("format", "json");

        // 2) 실제 API 호출 및 응답 수신
        InfoNaruPageResult result = infoNaruClient.getBookPage(params);
        List<BookIsbnTempDTO> books = (result == null) ? List.of() : result.getBooks();

        // 수신된 데이터가 없으면 경고 로그를 남기고 종료
        if (books == null || books.isEmpty()) {
            log.warn("정보나루 수집 실패: pageNo={} 건수=0", pageNo);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 3) 데이터 정제(Sanitization): 필수 필드(ISBN, 제목, 출판사) 누락 데이터 필터링
        books = books.stream()
                .filter(b -> b.getIsbn13() != null && !b.getIsbn13().isBlank())
                .filter(b -> b.getBookTitle() != null && !b.getBookTitle().isBlank())
                .filter(b -> b.getPublisher() != null && !b.getPublisher().isBlank())
                .toList();

        // 4) 메타데이터 설정: 수집 시각 및 초기 상태(0: NOTREADY) 부여
        books.forEach(book -> {
            book.setInfonaruFetchedAt(now);
            book.setStatusCd(0); // 초기 상태값 설정
        });

        // 5) 성능 최적화: 대량 Insert 시 DB 부하 및 락 타임아웃 방지를 위해 1,000건씩 분할 삽입(Chunking)

        int chunkSize = 1000;
        int inserted = 0;

        for (int i = 0; i < books.size(); i += chunkSize) {
            List<BookIsbnTempDTO> chunk =
                    books.subList(i, Math.min(i + chunkSize, books.size()));
            // DB에 데이터 적재 (중복 ISBN은 DB 제약 조건 또는 Mapper 로직에 의해 무시됨)
            inserted += bookBatchMapper.insertBookIsbnTemp(chunk);
        }

        // 6) 결과 분석: 수신된 건수와 실제 DB에 삽입된 건수 비교 (중복 건수 파악)
        int returned = books.size();
        int skipped = returned - inserted;

        // 최종 처리 현황 로깅 (운영 모니터링용)
        log.info(
                "InfoNaru 수집 완료: pageNo={}, 요청={}, 수신={}, 삽입={}, 스킵={}, 전체검색수={}",
                pageNo, pageSize, returned, inserted, skipped, result.getNumFound()
        );

        return true;
    }
}