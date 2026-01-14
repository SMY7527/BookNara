package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [InfoNaruDTO]
 * 정보나루(Data4Library) API 응답 도서 정보를 매핑하는 객체
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 응답 데이터의 불필요한 필드 무시
public class InfoNaruDTO {

    @JsonProperty("bookname") // API 필드명 'bookname'을 시스템 표준인 'booktitle'로 매핑
    private String booktitle;

    private String publisher;  // 출판사
    private String isbn13;     // 시스템 핵심 식별자(ISBN13)
}