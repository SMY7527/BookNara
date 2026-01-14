package com.booknara.booknaraPrj.report.controller;

import com.booknara.booknaraPrj.report.dto.ReportCreateDTO;
import com.booknara.booknaraPrj.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    private String userId(Authentication auth) {
        if (auth == null || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    @GetMapping("/exists")
    public ResponseEntity<?> exists(@RequestParam String feedId, Authentication auth) {
        String userId = userId(auth);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("code", "UNAUTHORIZED"));
        }

        boolean reported = reportService.hasReported(userId, feedId);
        return ResponseEntity.ok(Map.of("code", "OK", "reported", reported));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReportCreateDTO dto, Authentication auth) {
        String userId = userId(auth);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("code", "UNAUTHORIZED"));
        }

        try {
            reportService.createReport(userId, dto);
            return ResponseEntity.ok(Map.of("code", "OK"));

        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("본인 리뷰")) {
                return ResponseEntity.badRequest().body(Map.of("code", "SELF_REPORT_NOT_ALLOWED"));
            }
            return ResponseEntity.badRequest().body(Map.of("code", "ALREADY_REPORTED"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_TARGET"));
        }
    }
}

