package com.booknara.booknaraPrj.bookAPI.service.sync.naver.mapper;

import com.booknara.booknaraPrj.bookAPI.client.naver.NaverDTO;
import com.booknara.booknaraPrj.bookAPI.client.naver.NaverResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NaverTempMapper {

    public BookIsbnTempDTO toTempUpdateDto(String isbn13, NaverResponse apiResponse, LocalDateTime triedAt) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setNaverFetchedAt(triedAt);

        if (apiResponse == null || apiResponse.getItems() == null || apiResponse.getItems().isEmpty()) {
            return updateDto;
        }

        NaverDTO firstItem = apiResponse.getItems().get(0);
        updateDto.setAuthors(normalizeText(firstItem.getAuthor()));
        updateDto.setDescription(normalizeText(firstItem.getDescription()));
        updateDto.setNaverImage(normalizeText(firstItem.getImage()));

        return updateDto;
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

