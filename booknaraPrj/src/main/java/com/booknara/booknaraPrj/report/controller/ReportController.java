package com.booknara.booknaraPrj.report.controller;

import com.booknara.booknaraPrj.report.dto.ReportCreateDTO;
import com.booknara.booknaraPrj.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /** 이미 신고했는지 확인 */
    @GetMapping("/exists")
    public ResponseEntity<?> exists(@RequestParam String feedId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("code", "UNAUTHORIZED"));
        }

        boolean reported = reportService.hasReported(userId, feedId);

        return ResponseEntity.ok(Map.of(
                "code", "OK",
                "reported", reported
        ));
    }

    /** 신고 등록 */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReportCreateDTO dto, HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("code", "UNAUTHORIZED"));
        }

        try {
            reportService.createReport(userId, dto);
            return ResponseEntity.ok(Map.of("code", "OK"));

        } catch (IllegalStateException e) {
            // 이미 신고한 경우
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("code", "ALREADY_REPORTED"));

        } catch (IllegalArgumentException e) {
            // 존재하지 않거나 삭제된 리뷰
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("code", "INVALID_TARGET"));
        }
    }
}
