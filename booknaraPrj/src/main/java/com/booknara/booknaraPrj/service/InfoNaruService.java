package com.booknara.booknaraPrj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfoNaruService {

    private final InfoNaruPageImportService pageImportService;

    // 10만권 적재 (1만 * 10페이지)
    public void importTop100k() {
        int pageSize = 10_000;
        int totalPages = 10;

        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            boolean success = pageImportService.importOnePage(pageNo, pageSize);

            // 응답 0건 또는 실패 시 중단
            if (!success) {
                log.warn("import stopped at pageNo={}", pageNo);
                break;
            }
        }

        log.info("importTop100k finished");
    }
}
