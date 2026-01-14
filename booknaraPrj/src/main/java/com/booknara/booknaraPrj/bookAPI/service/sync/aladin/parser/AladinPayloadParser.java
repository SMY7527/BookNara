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

@Component
@Slf4j
public class AladinPayloadParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 무한루프(독성 payload) 방지용: 파싱 실패 예산
    private final ConcurrentHashMap<String, Integer> parseFailCount = new ConcurrentHashMap<>();
    private static final int PARSE_FAIL_BUDGET = 3;

    // flowdas 케이스 대응: JS object 에러 파싱
    private static final Pattern JS_ERRORCODE_PATTERN =
            Pattern.compile("errorCode\\s*[:=]\\s*['\"]?(\\d+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_ERRORMSG_PATTERN =
            Pattern.compile("errorMessage\\s*[:=]\\s*['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);

    public boolean looksLikeXml(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        return s.startsWith("<?xml") || s.startsWith("<");
    }

    public boolean looksLikeXmlError(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        return s.contains("<error") && s.contains("errorCode");
    }

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

    public AladinCallResult parseJsonToResult(String isbn13, String rawBody) {
        if (rawBody == null) return AladinCallResult.retryableFail("EMPTY_BODY");

        String normalized = normalizeAladinRaw(rawBody);
        if (normalized == null || normalized.isBlank()) return AladinCallResult.retryableFail("EMPTY_BODY");

        JsonNode root;
        try {
            root = objectMapper.readTree(normalized);
        } catch (Exception e) {
            Integer code = tryExtractErrorCode(normalized);
            String msg = tryExtractErrorMessage(normalized);

            if (code != null) {
                log.warn("aladin non-json error payload isbn13={} errorCode={} errorMessage={}", isbn13, code, msg);
                if (code == 10) return AladinCallResult.retryableFail("10");
                return AladinCallResult.retryableFail(String.valueOf(code));
            }

            log.warn("aladin json parse failed isbn13={} msg={}", isbn13, e.getMessage());
            int cnt = parseFailCount.merge(isbn13, 1, Integer::sum);
            if (cnt >= PARSE_FAIL_BUDGET) {
                log.error("aladin parse failed too many times -> nonRetry isbn13={} count={}", isbn13, cnt);
                parseFailCount.remove(isbn13);
                return AladinCallResult.nonRetryFail("JSON_PARSE_FAIL_BUDGET");
            }
            return AladinCallResult.retryableFail("JSON_PARSE_FAIL");
        }

        if (root.hasNonNull("errorCode")) {
            String code = root.path("errorCode").asText();
            String msg  = root.path("errorMessage").asText();
            log.warn("aladin error response isbn13={} errorCode={} errorMessage={}", isbn13, code, msg);

            if ("10".equals(code)) return AladinCallResult.retryableFail("10");
            if ("8".equals(code))  return AladinCallResult.retryableFail("8");
            return AladinCallResult.nonRetryFail(code);
        }

        JsonNode itemNode = root.path("item");
        if (!itemNode.isArray() || itemNode.isEmpty()) {
            parseFailCount.remove(isbn13);
            return AladinCallResult.noData();
        }

        try {
            AladinResponse resp = objectMapper.treeToValue(root, AladinResponse.class);
            parseFailCount.remove(isbn13);
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

    private String normalizeAladinRaw(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;

        while (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }

        int lparen = s.indexOf('(');
        int rparen = s.lastIndexOf(')');
        if (lparen > 0 && rparen > lparen && Character.isJavaIdentifierStart(s.charAt(0))) {
            String inside = s.substring(lparen + 1, rparen).trim();
            int comma = inside.indexOf(',');
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

    private Integer tryExtractErrorCode(String s) {
        Matcher m = JS_ERRORCODE_PATTERN.matcher(s);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignore) {}
        }
        return null;
    }

    private String tryExtractErrorMessage(String s) {
        Matcher m = JS_ERRORMSG_PATTERN.matcher(s);
        if (m.find()) return m.group(1);
        return null;
    }
}
