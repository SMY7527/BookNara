package com.booknara.booknaraPrj.bookAPI.client.aladin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 알라딘 API 최상위 응답 매핑 객체
 * 응답 상태 정보와 도서 데이터 리스트(item)를 포함함
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AladinResponse {
    private String errorCode;    // 응답 에러 코드 (정상 시 미출력)
    private String errorMessage; // 응답 에러 메시지
    private String version;      // API 버전 정보
    private List<AladinDTO> item; // 실제 도서 상세 정보 목록
}