package com.booknara.booknaraPrj.client.infoNaru;

import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InfoNaruPageResult {
    private final int pageNo; // 현재 페이지 번호
    private final int pageSize; //페이지별 데이터 수
    private final int numFound;     // 전체 건수
    private final List<BookIsbnTempDTO> books;
}
