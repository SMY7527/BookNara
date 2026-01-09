package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InfoNaruClient {

    // 정보나루 Open API 호출 전용 RestClient
    private final RestClient restClient;

    public InfoNaruClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://data4library.kr")
                .build();
    }

    // 도서 목록 조회 (메타 정보 불필요한 경우)
    public List<BookIsbnTempDTO> getBookList(Map<String, String> params) {
        InfoNaruResponse response = fetchResponse(params);

        if (response == null
                || response.getResponse() == null
                || response.getResponse().getDocs() == null) {
            return Collections.emptyList();
        }

        return response.getResponse().getDocs().stream()
                .map(InfoNaruResponse.DocWrapper::getDoc)
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());
    }

    // 외부 API DTO → 내부 도메인(BookDTO) 변환
    private BookIsbnTempDTO convertToBookDTO(InfoNaruDTO info) {
        BookIsbnTempDTO book = new BookIsbnTempDTO();
        book.setIsbn13(info.getIsbn13());
        book.setBookTitle(info.getBooktitle());
        book.setPublisher(info.getPublisher());

        return book;
    }


    // 정보나루 API 실제 HTTP 호출 담당
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

    // 페이지 단위 조회 (전체 건수 포함)
    public InfoNaruPageResult getBookPage(Map<String, String> params) {
        InfoNaruResponse response = fetchResponse(params);

        int pageNo = parseIntOrDefault(params.get("pageNo"), 1);
        int pageSize = parseIntOrDefault(params.get("pageSize"), 10);

        if (response == null || response.getResponse() == null) {
            return new InfoNaruPageResult(pageNo, pageSize, 0, List.of());
        }

        int numFound = response.getResponse().getNumFound();

        List<BookIsbnTempDTO> books = (response.getResponse().getDocs() == null)
                ? List.of()
                : response.getResponse().getDocs().stream()
                .map(InfoNaruResponse.DocWrapper::getDoc)
                .map(this::convertToBookDTO)
                .toList();

        return new InfoNaruPageResult(pageNo, pageSize, numFound, books);
    }

    // 문자열 → int 안전 변환
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value == null || value.isBlank())
                    ? defaultValue
                    : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
