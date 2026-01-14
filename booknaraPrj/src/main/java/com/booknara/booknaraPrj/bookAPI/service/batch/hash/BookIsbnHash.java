package com.booknara.booknaraPrj.bookAPI.service.batch.hash;

import com.booknara.booknaraPrj.bookAPI.domain.BookIsbnTempDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * [BookIsbnHash]
 * 도서 메타데이터 변경 감지용 SHA-256 해시 생성기
 * 데이터의 '동일성'을 비교하여 불필요한 DB 업데이트 작업을 방지함
 */
public final class BookIsbnHash {

    private BookIsbnHash() {} // 인스턴스화 방지

    /**
     * 주요 도서 정보 필드들을 결합하여 64자 해시값 생성
     * @return 데이터가 없으면 null, 있으면 SHA-256 Hex 문자열
     */
    public static String compute(BookIsbnTempDTO dto) {
        if (dto == null) return null;

        // 비교 대상 필드 규합 (도서명, 저자, 출판사, 설명, 출판일, 이미지들, 장르)
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

        if (source.replace("|", "").isEmpty()) return null;

        return sha256Hex(source);
    }

    /** 문자열 전처리: Null 처리, 양끝 공백 제거, 중복 공백 단일화 */
    private static String normalize(Object value) {
        if (value == null) return "";
        return value.toString().trim().replaceAll("[\\s\\t]+", " ");
    }

    /** SHA-256 해시 연산 핵심 로직 */
    private static String sha256Hex(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    /** 바이트 배열을 16진수 문자열로 변환 */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}