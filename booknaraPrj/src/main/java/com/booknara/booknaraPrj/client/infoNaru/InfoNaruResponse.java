package com.booknara.booknaraPrj.client.infoNaru;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

//정보나루 API 응답 매핑용 DTO
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoNaruResponse {

    //응답 데이터 영역
    private ResponseData response;

    // 전체 건수 + 도서 목록
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        private int numFound;              // 전체 검색 결과 수
        private List<DocWrapper> docs;     // 도서 목록
    }

    //docs 배열 내 실제 도서 래퍼
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocWrapper {
        private InfoNaruDTO doc;           // 도서 정보
    }
}
