package com.booknara.booknaraPrj.bookAPI.service.sync.naver.mapper;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverDTO;
import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * [NaverTempMapper]
 * 네이버 도서 검색 API의 응답 데이터를 시스템 Staging DTO(TEMP)로 변환하는 컴포넌트입니다.
 * 외부 API의 필드명을 시스템 내부 도메인 필드명으로 매핑하고 데이터를 정규화합니다.
 */
@Component
public class NaverTempMapper {

    /**
     * 네이버 API 응답 객체를 업데이트용 DTO로 변환합니다.
     * @param isbn13 조회 기준이 된 ISBN
     * @param apiResponse 네이버 API 호출 결과 본문
     * @param triedAt 호출 시도 시각 (수집 이력 관리용)
     * @return 보강된 정보가 담긴 BookIsbnTempDTO
     */
    public BookIsbnTempDTO toTempUpdateDto(String isbn13, NaverResponse apiResponse, LocalDateTime triedAt) {
        // 1. 기본 식별 정보 및 수집 시각 설정
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setNaverFetchedAt(triedAt);

        // 2. 응답 데이터 존재 여부 검증 (데이터가 없어도 시도 시각 기록을 위해 DTO 반환)
        if (apiResponse == null || apiResponse.getItems() == null || apiResponse.getItems().isEmpty()) {
            return updateDto;
        }

        // [이미지: 네이버 API 응답 필드와 시스템 DTO 필드 간의 매핑 구조]
        //

        // 3. 첫 번째 검색 결과(가장 정확도가 높은 아이템)를 기준으로 데이터 보강
        NaverDTO firstItem = apiResponse.getItems().get(0);

        // 데이터 정규화(Normalize)를 거쳐 필드 매핑 수행
        updateDto.setAuthors(normalizeText(firstItem.getAuthor()));       // 저자 정보 보강
        updateDto.setDescription(normalizeText(firstItem.getDescription())); // 상세 설명 보강
        updateDto.setNaverImage(normalizeText(firstItem.getImage()));     // 네이버 표지 이미지 URL

        return updateDto;
    }

    /**
     * 문자열 데이터를 시스템 표준에 맞게 정규화합니다.
     * - Null 처리 및 양끝 공백 제거(Trim)
     * - 빈 문자열("")인 경우 DB 가독성을 위해 null로 치환
     */
    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}