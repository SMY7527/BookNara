package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.mapper;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class AladinTempMapper {

    public BookIsbnTempDTO toTempUpdateDto(String isbn13, AladinResponse apiResponse, LocalDateTime triedAt) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setAladinFetchedAt(triedAt);

        if (apiResponse == null || apiResponse.getItem() == null || apiResponse.getItem().isEmpty()) {
            return updateDto;
        }

        AladinDTO firstItem = apiResponse.getItem().get(0);

        updateDto.setPubdate(parsePubdateToYyyyMMdd(firstItem.getPubdate())); // ✅ 핵심
        updateDto.setGenreId(parseInteger(firstItem.getCategoryId()));
        updateDto.setAladinImageBig(normalizeText(firstItem.getCover()));
        updateDto.setDescription(normalizeText(firstItem.getDescription()));

        return updateDto;
    }

    private String parsePubdateToYyyyMMdd(String pubdate) {
        String value = normalizeText(pubdate);
        if (value == null) return null;

        // 이미 yyyyMMdd
        if (value.matches("\\d{8}")) {
            return value;
        }

        // yyyy-MM-dd → yyyyMMdd
        try {
            LocalDate d = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return d.format(DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ignore) {}

        return null;
    }

    private Integer parseInteger(String numberText) {
        String value = normalizeText(numberText);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

