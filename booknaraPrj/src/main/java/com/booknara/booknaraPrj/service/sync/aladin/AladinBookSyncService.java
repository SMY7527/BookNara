package com.booknara.booknaraPrj.service.sync.aladin;

import com.booknara.booknaraPrj.client.aladin.AladinClient;
import com.booknara.booknaraPrj.client.aladin.AladinDTO;
import com.booknara.booknaraPrj.client.aladin.AladinResponse;
import com.booknara.booknaraPrj.domain.BookIsbnTempDTO;
import com.booknara.booknaraPrj.domain.ResponseStatus;
import com.booknara.booknaraPrj.mapper.BookBatchMapper;
import com.booknara.booknaraPrj.service.batch.hash.BookIsbnHash;
import com.booknara.booknaraPrj.service.policy.TempReadyPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AladinBookSyncService {

    private final BookBatchMapper batchMapper;
    private final AladinClient aladinClient;
    private final TempReadyPolicy readyPolicy;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================
    // 튜닝 포인트
    // =========================
    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF_MS = 200;

    private static final long KEY_COOLDOWN_ON_429_MS = 15_000; // 15초
    private static final long KEY_COOLDOWN_ON_5XX_MS = 5_000;  // 5초

    private static final int DAILY_429_COUNT_LIMIT = 3;
    private final ConcurrentHashMap<Integer, Integer> daily429Count = new ConcurrentHashMap<>();

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long DELAY_MINUTES = 5;

    public int syncOnce(int limit) {
        List<String> targetIsbnList = batchMapper.selectTempIsbnForAladin(limit);

        if (targetIsbnList == null || targetIsbnList.isEmpty()) {
            log.info("aladin sync: no targets");
            return 0;
        }

        int successCount = 0;
        for (String isbn13 : targetIsbnList) {
            if (syncOne(isbn13)) successCount++;
        }

        log.info("aladin sync: success={}/{}", successCount, targetIsbnList.size());
        return targetIsbnList.size();
    }

    public void syncLoop(int limit) {
        while (true) {
            int targetCount = syncOnce(limit);
            if (targetCount == 0) break;
        }
        log.info("aladin syncLoop finished");
    }

    /**
     * 알라딘 보강
     * - ALADIN_FETCHED_AT: 시도하면 무조건 기록
     * - ALADIN_RES_STATUS: 응답 결과에 따라 기록
     * - 데이터가 있으면 pubdate/genreId/cover/description 업데이트
     */
    @Transactional
    public boolean syncOne(String isbn13) {
        LocalDateTime triedAt = LocalDateTime.now();

        try {
            AladinCallResult result = fetchWithRetry(isbn13);

            // 1) 시도 시각 + 상태 저장은 "항상"
            batchMapper.updateTempAladinMeta(
                    isbn13,
                    triedAt,
                    result.status.getCode()
            );

            // 2) 데이터가 있는 성공일 때만 실제 데이터 업데이트
            if (result.status == ResponseStatus.SUCCESS_WITH_DATA) {
                BookIsbnTempDTO tempUpdate = toTempUpdateDto(isbn13, result.response, triedAt);
                batchMapper.updateTempFromAladin(tempUpdate);
            }

            // 3) READY 판정 (최종 병합 상태 기준)
            BookIsbnTempDTO mergedTemp = batchMapper.selectTempByIsbn13(isbn13);
            if (mergedTemp != null && readyPolicy.isReady(mergedTemp)) {
                String newHash = BookIsbnHash.compute(mergedTemp);
                batchMapper.updateTempDataHash(isbn13, newHash);
                batchMapper.markTempReady(isbn13);
            }

            return result.status == ResponseStatus.SUCCESS_WITH_DATA;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("aladin sync interrupted isbn13={}", isbn13);
            return false;

        } catch (Exception ex) {
            log.warn("aladin sync failed (unexpected) isbn13={}", isbn13, ex);

            try {
                batchMapper.updateTempAladinMeta(
                        isbn13,
                        triedAt,
                        ResponseStatus.RETRYABLE_FAIL.getCode()
                );
            } catch (Exception ignore) {}

            return false;

        } finally {
            try {
                Thread.sleep(120);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 알라딘 호출 + 정책 적용 (RAW 기반)
     * - XML <error> 응답: errorCode 파싱 후 분류
     * - JSON 응답:
     *    - errorCode 있으면 에러 응답으로 분기(= NO_DATA 아님!)
     *    - item 비어있으면 NO_DATA
     *    - item 있으면 WITH_DATA
     * - HTTP 429/5xx/네트워크/파싱: RETRYABLE_FAIL
     * - 401/403/기타 4xx: NONRETRY_FAIL(보통)
     */
    private AladinCallResult fetchWithRetry(String isbn13) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            Integer keyIdx = null;

            try {
                String raw = aladinClient.searchByIsbnOnceRaw(isbn13);
                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // 원인 확인용: 너무 많이 찍히면 로그 폭발하니 첫 시도에서만(필요 시 주석 해제)
                // if (attempt == 1) log.warn("ALADIN RAW isbn13={} raw={}", isbn13, raw);

                // (A) XML 에러 응답 처리
                if (looksLikeXml(raw)) {
                    if (looksLikeXmlError(raw)) {
                        int errorCode = parseXmlErrorCode(raw);

                        // 10: 일일 쿼리 초과 (오늘 소진)
                        if (errorCode == 10) {
                            if (keyIdx != null) {
                                long until = getNextScheduledTime();
                                aladinClient.cooldownKeyUntil(keyIdx, until);
                                log.warn("aladin daily limit exceeded(errorCode=10). keyIdx={} until={} isbn13={}",
                                        keyIdx, until, isbn13);
                            }
                            return AladinCallResult.retryableFail(); // 오늘은 사실상 불가지만 분류상 retryable
                        }

                        // 그 외 XML 에러는 기본적으로 non-retry
                        log.warn("aladin xml error. errorCode={} isbn13={}", errorCode, isbn13);
                        return AladinCallResult.nonRetryFail();
                    }

                    // XML인데 <error>가 아닌 경우(예: 다른 형식) -> 보수적으로 retryable
                    log.warn("aladin got xml but not <error>. isbn13={} attempt={}", isbn13, attempt);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // (B) JSON 응답: JsonNode로 선분기 후 결과 확정
                AladinCallResult parsed = parseJsonToResult(isbn13, raw);

                // 성공 시 429 누적 리셋(키 기준)
                if (keyIdx != null && parsed.status == ResponseStatus.SUCCESS_WITH_DATA) {
                    daily429Count.remove(keyIdx);
                }

                return parsed;

            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                keyIdx = aladinClient.getLastKeyIndex();
                aladinClient.clearLastKeyIndex();

                // 429
                if (statusCode == 429) {
                    if (keyIdx != null) {
                        int count = daily429Count.merge(keyIdx, 1, Integer::sum);

                        if (count >= DAILY_429_COUNT_LIMIT) {
                            long until = getNextScheduledTime();
                            aladinClient.cooldownKeyUntil(keyIdx, until);
                            daily429Count.remove(keyIdx);

                            log.warn("aladin key exhausted(429). keyIdx={} until={} isbn13={}", keyIdx, until, isbn13);
                            return AladinCallResult.retryableFail();
                        }

                        aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_429_MS);
                    }

                    log.warn("aladin 429 rate-limited isbn13={} attempt={}/{}", isbn13, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 5xx
                if (statusCode >= 500 && statusCode <= 599) {
                    if (keyIdx != null) aladinClient.cooldownKey(keyIdx, KEY_COOLDOWN_ON_5XX_MS);
                    log.warn("aladin 5xx server error isbn13={} status={} attempt={}/{}",
                            isbn13, statusCode, attempt, MAX_RETRY_COUNT);
                    Thread.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 3000);
                    continue;
                }

                // 401/403 -> 재시도 불가(보통)
                if (statusCode == 401 || statusCode == 403) {
                    log.error("aladin auth/forbidden isbn13={} status={}", isbn13, statusCode);
                    return AladinCallResult.nonRetryFail();
                }

                // 기타 4xx
                log.warn("aladin non-retry client error isbn13={} status={}", isbn13, statusCode);
                return AladinCallResult.nonRetryFail();

            } catch (Exception ex) {
                // 파싱/네트워크 등 -> 재시도
                log.warn("aladin unexpected error isbn13={} attempt={}/{} msg={}",
                        isbn13, attempt, MAX_RETRY_COUNT, ex.getMessage());
                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 3000);
            }
        }

        return AladinCallResult.retryableFail();
    }

    // =========================
    // RAW 판별/파싱 유틸
    // =========================

    private boolean looksLikeXml(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        return s.startsWith("<?xml") || s.startsWith("<");
    }

    private boolean looksLikeXmlError(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        // <?xml ...> 포함 여부에 의존하지 말고 <error 태그 존재를 더 넓게 잡음
        return s.contains("<error") && s.contains("errorCode");
    }

    private int parseXmlErrorCode(String rawXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
            String text = doc.getElementsByTagName("errorCode").item(0).getTextContent();
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            // 에러 응답인데 코드 파싱 실패 -> 보수적으로 -1
            return -1;
        }
    }

    /**
     * JSON 응답을 먼저 JsonNode로 파싱해서
     * 1) errorCode 존재 여부(=에러 응답) 판별
     * 2) item 존재/비어있음 판별
     * 3) 그 다음에만 DTO로 변환
     *
     * 이 방식이 "데이터 못 받았는데 NO_DATA(2)로 찍히는 오판"을 막는다.
     */
    private AladinCallResult parseJsonToResult(String isbn13, String rawJson) throws Exception {
        if (rawJson == null) return AladinCallResult.retryableFail();

        String s = rawJson.trim();
        if (s.isEmpty()) return AladinCallResult.retryableFail();

        JsonNode root;
        try {
            root = objectMapper.readTree(s);
        } catch (Exception e) {
            log.warn("aladin json parse failed isbn13={} msg={}", isbn13, e.getMessage());
            return AladinCallResult.retryableFail();
        }

        if (root.hasNonNull("errorCode")) {
            String code = root.path("errorCode").asText();
            String msg  = root.path("errorMessage").asText();

            log.warn("aladin error response isbn13={} errorCode={} errorMessage={}", isbn13, code, msg);

            // 10 = 일일 쿼터 초과 → 내일 다시 시도해야 하니까 retryable
            if ("10".equals(code)) {
                return AladinCallResult.retryableFail();
            }

            return AladinCallResult.nonRetryFail();
        }

        // item 비어있으면 진짜 NO_DATA
        JsonNode itemNode = root.path("item");
        if (!itemNode.isArray() || itemNode.isEmpty()) {
            return AladinCallResult.noData();
        }

        // 여기서만 DTO 변환
        AladinResponse resp = objectMapper.treeToValue(root, AladinResponse.class);
        return AladinCallResult.withData(resp);
    }


    private long getNextScheduledTime() {
        return ZonedDateTime.now(KST_ZONE)
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(KST_ZONE)
                .plusMinutes(DELAY_MINUTES)
                .toInstant()
                .toEpochMilli();
    }

    // =========================
    // TEMP 업데이트 DTO 변환
    // =========================

    private BookIsbnTempDTO toTempUpdateDto(String isbn13, AladinResponse apiResponse, LocalDateTime triedAt) {
        BookIsbnTempDTO updateDto = new BookIsbnTempDTO();
        updateDto.setIsbn13(isbn13);
        updateDto.setAladinFetchedAt(triedAt);

        if (apiResponse == null || apiResponse.getItem() == null || apiResponse.getItem().isEmpty()) {
            return updateDto;
        }

        AladinDTO firstItem = apiResponse.getItem().get(0);

        updateDto.setPubdate(parsePubdate(firstItem.getPubdate()));
        updateDto.setGenreId(parseInteger(firstItem.getCategoryId()));
        updateDto.setAladinImageBig(normalizeText(firstItem.getCover()));
        updateDto.setDescription(normalizeText(firstItem.getDescription()));

        return updateDto;
    }

    private LocalDate parsePubdate(String pubdate) {
        String value = normalizeText(pubdate);
        if (value == null) return null;

        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignore) {}

        try {
            DateTimeFormatter compact = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(value, compact);
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

    // =========================================================
    // 내부 결과 객체: status + response
    // =========================================================
    private static class AladinCallResult {
        final ResponseStatus status;
        final AladinResponse response;

        private AladinCallResult(ResponseStatus status, AladinResponse response) {
            this.status = status;
            this.response = response;
        }

        static AladinCallResult withData(AladinResponse response) {
            return new AladinCallResult(ResponseStatus.SUCCESS_WITH_DATA, response);
        }

        static AladinCallResult noData() {
            return new AladinCallResult(ResponseStatus.SUCCESS_NO_DATA, null);
        }

        static AladinCallResult retryableFail() {
            return new AladinCallResult(ResponseStatus.RETRYABLE_FAIL, null);
        }

        static AladinCallResult nonRetryFail() {
            return new AladinCallResult(ResponseStatus.NONRETRY_FAIL, null);
        }
    }
}
