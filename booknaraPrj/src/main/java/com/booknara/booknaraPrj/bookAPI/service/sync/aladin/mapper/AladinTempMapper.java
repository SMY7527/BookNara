package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.mapper;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * [AladinTempMapper]
 * 알라딘 API 응답 객체(Response)를 시스템 Staging DTO로 변환하는 매퍼 컴포넌트
 */
@Component
public class AladinTempMapper {

    /**
     * 알라딘 API 결과로부터 업데이트에 필요한 필드만 추출하여 DTO 생성
     * @param isbn13 조회 대상 식별자
     * @param apiResponse 알라딘 API 응답 데이터
     * @param triedAt 호출 시도 시각 (Metadata)
     */
    public BookIsbnTempDTO toTempUpdateDto(String isbn13, AladinResponse apiResponse, LocalDateTime triedAt) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setAladinFetchedAt(triedAt); // 수집 시각 기록

        // 응답이 없거나 아이템 리스트가 비어있는 경우 기본 정보만 반환 (이후 ResStatus 처리를 위함)
        if (apiResponse == null || apiResponse.getItem() == null || apiResponse.getItem().isEmpty()) {
            return updateDto;
        }

        // 알라딘 ItemLookUp API는 ISBN 검색 시 단일 건을 반환하므로 첫 번째 아이템 사용
        AladinDTO firstItem = apiResponse.getItem().get(0);

        // --- 데이터 정규화 및 필드 매핑 ---
        updateDto.setPubdate(parsePubdateToYyyyMMdd(firstItem.getPubdate())); // 출판일 규격화
        updateDto.setGenreId(parseInteger(firstItem.getCategoryId()));       // 장르 ID 변환
        updateDto.setAladinImageBig(normalizeText(firstItem.getCover()));    // 표지 URL 정규화
        updateDto.setDescription(normalizeText(firstItem.getDescription())); // 도서 설명 정규화

        return updateDto;
    }

    /**
     * 출판일 형식을 시스템 표준(yyyyMMdd)으로 변환
     * - 케이스1: 20240101 (이미 표준인 경우)
     * - 케이스2: 2024-01-01 (ISO 날짜 형식)
     */
    private String parsePubdateToYyyyMMdd(String pubdate) {
        String value = normalizeText(pubdate);
        if (value == null) return null;

        // 숫자 8자리 포맷인 경우 그대로 반환
        if (value.matches("\\d{8}")) {
            return value;
        }

        // yyyy-MM-dd 형태인 경우 하이픈 제거 후 yyyyMMdd로 변환
        try {
            LocalDate d = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return d.format(DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ignore) {}

        return null; // 변환 불가 시 null (데이터 품질을 위해 잘못된 값은 저장하지 않음)
    }

    /** 문자열 형태의 ID를 안전하게 Integer로 변환 */
    private Integer parseInteger(String numberText) {
        String value = normalizeText(numberText);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 문자열 전처리: 공백 제거 및 빈 문자열을 null로 치환하여 DB 정합성 확보 */
    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}