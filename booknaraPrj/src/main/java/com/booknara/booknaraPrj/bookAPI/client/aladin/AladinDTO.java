package com.booknara.booknaraPrj.bookAPI.client.aladin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알라딘 API 응답 데이터 매핑 DTO
 * 외부 데이터를 BOOK_ISBN_TEMP 테이블로 이관하기 전 임시 수용 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 정의되지 않은 필드는 무시하여 파싱 에러 방지
public class AladinDTO {

    @JsonProperty("pubDate") // 외부 API 카멜케이스 대응
    private String pubdate;

    private String cover;        // 도서 표지 URL
    private String categoryId;   // 내부 GENRE 매핑용 ID
    private String description;  // 도서 요약 설명
}