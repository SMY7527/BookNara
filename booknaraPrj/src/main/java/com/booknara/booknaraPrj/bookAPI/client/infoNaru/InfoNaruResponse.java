package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * [InfoNaruResponse]
 * 정보나루 API의 최상위 응답 포맷 매핑 객체
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoNaruResponse {

    private ResponseData response; // 응답 데이터 본문

    /** 검색 결과 요약 및 목록을 포함하는 데이터 영역 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        private int numFound;          // 검색 조건에 맞는 전체 도서 수
        private List<DocWrapper> docs; // 도서 정보 래퍼 리스트
    }

    /** * 개별 도서 정보를 감싸고 있는 래퍼 클래스
     * API의 { "doc": { ... } } 구조 대응용
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocWrapper {
        private InfoNaruDTO doc;       // 실제 도서 상세 정보 (제목, 저자, ISBN 등)
    }
}