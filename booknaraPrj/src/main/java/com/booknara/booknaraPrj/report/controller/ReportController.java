package com.booknara.booknaraPrj.report.controller;

import com.booknara.booknaraPrj.report.dto.ReportCreateDTO;
import com.booknara.booknaraPrj.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * [ReportController]
 * 리뷰 및 피드 신고 기능을 담당하는 REST 컨트롤러입니다.
 * 모든 응답은 JSON 형태로 제공되며, 상황별 에러 코드를 통해 프론트엔드의 대응을 돕습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /** [유틸리티] Authentication 객체에서 안전하게 사용자 ID를 추출합니다. */
    private String userId(Authentication auth) {
        if (auth == null || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null; // 로그인하지 않은 경우 null 반환
        }
        return auth.getName();
    }

    /**
     * [신고 여부 조회]
     * GET /api/reports/exists?feedId=...
     * 특정 리뷰를 이미 신고했는지 확인하여 버튼 비활성화 등의 UI 제어에 사용됩니다.
     */
    @GetMapping("/exists")
    public ResponseEntity<?> exists(@RequestParam String feedId, Authentication auth) {
        String userId = userId(auth);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("code", "UNAUTHORIZED"));
        }

        boolean reported = reportService.hasReported(userId, feedId);
        return ResponseEntity.ok(Map.of("code", "OK", "reported", reported));
    }

    /**
     * [신고 접수]
     * POST /api/reports
     * 사용자로부터 신고 사유를 받아 접수합니다. 비즈니스 로직 위반 시 상황별 코드를 반환합니다.
     */
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
            // 본인 리뷰 신고 시도 시 전용 코드 반환
            if (e.getMessage() != null && e.getMessage().contains("본인 리뷰")) {
                return ResponseEntity.badRequest().body(Map.of("code", "SELF_REPORT_NOT_ALLOWED"));
            }
            // 중복 신고 시 코드 반환
            return ResponseEntity.badRequest().body(Map.of("code", "ALREADY_REPORTED"));

        } catch (IllegalArgumentException e) {
            // 대상 피드가 존재하지 않거나 삭제된 경우
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_TARGET"));
        }
    }
}