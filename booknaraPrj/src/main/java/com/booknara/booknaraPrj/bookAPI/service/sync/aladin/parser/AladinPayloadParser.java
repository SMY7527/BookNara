package com.booknara.booknaraPrj.bookAPI.service.sync.aladin.parser;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.bookAPI.service.sync.aladin.model.AladinCallResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [AladinPayloadParser]
 * 알라딘 API의 비표준 응답 형식을 분석하고 정규화하여 시스템 표준 객체로 변환합니다.
 */
@Component
@Slf4j
public class AladinPayloadParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 특정 ISBN에 대한 파싱 실패 횟수를 추적하여 무한 재시도(자원 낭비) 방지
    private final ConcurrentHashMap<String, Integer> parseFailCount = new ConcurrentHashMap<>();
    private static final int PARSE_FAIL_BUDGET = 3; // ISBN당 최대 파싱 시도 횟수

    // JSON 파싱 실패 시, JavaScript 객체 형태의 에러 메시지에서 정보를 강제 추출하기 위한 패턴
    private static final Pattern JS_ERRORCODE_PATTERN =
            Pattern.compile("errorCode\\s*[:=]\\s*['\"]?(\\d+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_ERRORMSG_PATTERN =
            Pattern.compile("errorMessage\\s*[:=]\\s*['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);

    /** 응답 본문이 XML 형식(주로 에러 발생 시 XML로 반환됨)인지 확인 */
    public boolean looksLikeXml(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        return s.startsWith("<?xml") || s.startsWith("<");
    }

    /** XML 응답 중 에러 정의 태그 포함 여부 확인 */
    public boolean looksLikeXmlError(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        return s.contains("<error") && s.contains("errorCode");
    }

    /** XML 에러 본문에서 errorCode 값 추출 */
    public int parseXmlErrorCode(String rawXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
            String text = doc.getElementsByTagName("errorCode").item(0).getTextContent();
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 핵심 파싱 로직: Raw 데이터를 분석하여 최종 결과 객체(AladinCallResult) 생성
     */
    public AladinCallResult parseJsonToResult(String isbn13, String rawBody) {
        if (rawBody == null) return AladinCallResult.retryableFail("EMPTY_BODY");

        // [1단계] 데이터 정규화: JS Callback 래퍼 등을 제거하여 순수 JSON 확보
        String normalized = normalizeAladinRaw(rawBody);
        if (normalized == null || normalized.isBlank()) return AladinCallResult.retryableFail("EMPTY_BODY");

        JsonNode root;
        try {
            // [2단계] JSON 트리 구조 생성
            root = objectMapper.readTree(normalized);
        } catch (Exception e) {
            // JSON 파싱 실패 시: 정규표현식을 통해 에러 코드 직접 추출 시도 (Flowdas 등 변칙 케이스 대응)
            Integer code = tryExtractErrorCode(normalized);
            String msg = tryExtractErrorMessage(normalized);

            if (code != null) {
                log.warn("aladin non-json error payload isbn13={} errorCode={} errorMessage={}", isbn13, code, msg);
                if (code == 10) return AladinCallResult.retryableFail("10"); // 일시적 서버 오류
                return AladinCallResult.retryableFail(String.valueOf(code));
            }

            // 파싱 실패 예산(Budget) 차감 및 재시도 여부 결정
            log.warn("aladin json parse failed isbn13={} msg={}", isbn13, e.getMessage());
            int cnt = parseFailCount.merge(isbn13, 1, Integer::sum);
            if (cnt >= PARSE_FAIL_BUDGET) {
                log.error("aladin parse failed too many times -> nonRetry isbn13={} count={}", isbn13, cnt);
                parseFailCount.remove(isbn13);
                return AladinCallResult.nonRetryFail("JSON_PARSE_FAIL_BUDGET");
            }
            return AladinCallResult.retryableFail("JSON_PARSE_FAIL");
        }

        // [3단계] API 명시적 에러 처리 (errorCode 필드 존재 시)
        if (root.hasNonNull("errorCode")) {
            String code = root.path("errorCode").asText();
            String msg  = root.path("errorMessage").asText();
            log.warn("aladin error response isbn13={} errorCode={} errorMessage={}", isbn13, code, msg);

            // 10: 일시적 오류, 8: 한도 초과 등 재시도 가능 케이스 구분
            if ("10".equals(code)) return AladinCallResult.retryableFail("10");
            if ("8".equals(code))  return AladinCallResult.retryableFail("8");
            return AladinCallResult.nonRetryFail(code);
        }

        // [4단계] 데이터 유무 확인 (item 배열 비어있는지 체크)
        JsonNode itemNode = root.path("item");
        if (!itemNode.isArray() || itemNode.isEmpty()) {
            parseFailCount.remove(isbn13);
            return AladinCallResult.noData();
        }

        // [5단계] 최종 DTO 매핑
        try {
            AladinResponse resp = objectMapper.treeToValue(root, AladinResponse.class);
            parseFailCount.remove(isbn13); // 성공 시 실패 카운트 초기화
            return AladinCallResult.withData(resp);
        } catch (Exception e) {
            log.warn("aladin dto mapping failed isbn13={} msg={}", isbn13, e.getMessage());
            int cnt = parseFailCount.merge(isbn13, 1, Integer::sum);
            if (cnt >= PARSE_FAIL_BUDGET) {
                log.error("aladin dto mapping failed too many times -> nonRetry isbn13={} count={}", isbn13, cnt);
                parseFailCount.remove(isbn13);
                return AladinCallResult.nonRetryFail("DTO_MAPPING_FAIL_BUDGET");
            }
            return AladinCallResult.retryableFail("DTO_MAPPING_FAIL");
        }
    }

    public void clearParseFail(String isbn13) {
        parseFailCount.remove(isbn13);
    }

    /**
     * 알라딘 특유의 JavaScript Callback( 함수명({json}) ) 규격에서 순수 JSON 본문만 추출합니다.
     */
    private String normalizeAladinRaw(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;

        // 세미콜론 제거
        while (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }

        // callback(...) 형태의 문자열에서 괄호 안쪽 내용만 추출
        int lparen = s.indexOf('(');
        int rparen = s.lastIndexOf(')');
        if (lparen > 0 && rparen > lparen && Character.isJavaIdentifierStart(s.charAt(0))) {
            String inside = s.substring(lparen + 1, rparen).trim();
            int comma = inside.indexOf(',');
            // 특정 알라딘 API 규격에서 파라미터가 여러 개인 경우 JSON 본문 위치 탐색
            if (comma > 0) {
                String payload = inside.substring(comma + 1).trim();
                while (payload.endsWith(";")) {
                    payload = payload.substring(0, payload.length() - 1).trim();
                }
                return payload;
            }
        }
        return s;
    }

    /** 텍스트 데이터에서 정규표현식으로 errorCode 추출 */
    private Integer tryExtractErrorCode(String s) {
        Matcher m = JS_ERRORCODE_PATTERN.matcher(s);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignore) {}
        }
        return null;
    }

    /** 텍스트 데이터에서 정규표현식으로 errorMessage 추출 */
    private String tryExtractErrorMessage(String s) {
        Matcher m = JS_ERRORMSG_PATTERN.matcher(s);
        if (m.find()) return m.group(1);
        return null;
    }
}