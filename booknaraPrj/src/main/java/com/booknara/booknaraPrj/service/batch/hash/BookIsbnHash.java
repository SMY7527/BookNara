package com.booknara.booknaraPrj.service.batch.hash;

import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 도서 ISBN 메타 데이터 변경 감지용 해시 생성기
 * - 보안 목적 아님
 * - TEMP → READY 승격 전 최종 상태 기준
 * - InfoNaru + Naver + Aladin 결과를 모두 포함
 */
public final class BookIsbnHash {

    private BookIsbnHash() {}

    /**
     * TEMP 테이블의 "최종 상태" 기준으로 DATA_HASH 생성
     *
     * @return
     *  - 의미 있는 데이터가 없으면 null
     *  - 그렇지 않으면 SHA-256 hex (64자)
     */
    public static String compute(BookIsbnTempDTO dto) {
        if (dto == null) {
            return null;
        }

        String source = String.join("|",
                normalize(dto.getBookTitle()),
                normalize(dto.getAuthors()),
                normalize(dto.getPublisher()),
                normalize(dto.getDescription()),
                normalize(dto.getPubdate()),
                normalize(dto.getNaverImage()),
                normalize(dto.getAladinImageBig()),
                normalize(dto.getGenreId())
        );

        // 전부 비어있으면 의미 없음
        if (source.replace("|", "").isEmpty()) {
            return null;
        }

        return sha256Hex(source);
    }

    /* ------------------ normalize helpers ------------------ */

    private static String normalize(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .trim()
                .replaceAll("[\\s\\t]+", " ");
    }

    /* ------------------ hash core ------------------ */

    private static String sha256Hex(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
