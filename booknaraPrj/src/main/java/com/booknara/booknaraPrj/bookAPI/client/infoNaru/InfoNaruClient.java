package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * [InfoNaruClient]
 * 정보나루(Data4Library) Open API 통신 및 데이터 변환 담당
 */
@Component
public class InfoNaruClient {

    private final RestClient restClient;

    public InfoNaruClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://data4library.kr") // 정보나루 API 베이스 URL
                .build();
    }

    /** 도서 목록 단순 조회 (가공된 리스트만 반환) */
    public List<BookIsbnTempDTO> getBookList(Map<String, String> params) {
        InfoNaruResponse response = fetchResponse(params);

        if (response == null || response.getResponse() == null || response.getResponse().getDocs() == null) {
            return Collections.emptyList();
        }

        return response.getResponse().getDocs().stream()
                .map(InfoNaruResponse.DocWrapper::getDoc)
                .map(this::convertToBookDTO) // 내부 규격으로 변환
                .collect(Collectors.toList());
    }

    /** 외부 API DTO를 시스템 표준 Staging DTO(BookIsbnTempDTO)로 매핑 */
    private BookIsbnTempDTO convertToBookDTO(InfoNaruDTO info) {
        BookIsbnTempDTO book = new BookIsbnTempDTO();
        book.setIsbn13(info.getIsbn13());
        book.setBookTitle(info.getBooktitle());
        book.setPublisher(info.getPublisher());
        return book;
    }

    /** 실제 HTTP GET 요청 수행 */
    private InfoNaruResponse fetchResponse(Map<String, String> params) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/srchBooks");
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(InfoNaruResponse.class);
    }

    /** 페이지 단위 조회 (전체 건수와 페이징 정보를 포함한 결과 반환) */
    public InfoNaruPageResult getBookPage(Map<String, String> params) {
        InfoNaruResponse response = fetchResponse(params);

        int pageNo = parseIntOrDefault(params.get("pageNo"), 1);
        int pageSize = parseIntOrDefault(params.get("pageSize"), 10);

        if (response == null || response.getResponse() == null) {
            return new InfoNaruPageResult(pageNo, pageSize, 0, List.of());
        }

        int numFound = response.getResponse().getNumFound(); // 전체 검색 결과 수 추출

        List<BookIsbnTempDTO> books = (response.getResponse().getDocs() == null)
                ? List.of()
                : response.getResponse().getDocs().stream()
                .map(InfoNaruResponse.DocWrapper::getDoc)
                .map(this::convertToBookDTO)
                .toList();

        return new InfoNaruPageResult(pageNo, pageSize, numFound, books);
    }

    /** 쿼리 파라미터(문자열)를 안전하게 정수로 변환 */
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}