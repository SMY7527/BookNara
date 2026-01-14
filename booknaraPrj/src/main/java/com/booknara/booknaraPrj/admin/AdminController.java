package com.booknara.booknaraPrj.admin;

import com.booknara.booknaraPrj.admin.Event.AdminEvent;
import com.booknara.booknaraPrj.admin.Event.AdminEventService;
import com.booknara.booknaraPrj.admin.bookManagement.*;
import com.booknara.booknaraPrj.admin.inquiry.*;
import com.booknara.booknaraPrj.admin.notifications.AdminNotiResponseDto;
import com.booknara.booknaraPrj.admin.notifications.AdminNotification;
import com.booknara.booknaraPrj.admin.notifications.AdminNotificationService;
import com.booknara.booknaraPrj.admin.recomBooks.AdminBookSearchResponseDto;
import com.booknara.booknaraPrj.admin.recomBooks.AdminRecomSaveRequestDto;
import com.booknara.booknaraPrj.admin.report.AdminReport;
import com.booknara.booknaraPrj.admin.report.AdminReportService;
import com.booknara.booknaraPrj.admin.report.AdminReportState;
import com.booknara.booknaraPrj.admin.settings.AdminSettings;
import com.booknara.booknaraPrj.admin.settings.AdminSettingsService;
import com.booknara.booknaraPrj.admin.statistics.StatisticsService;
import com.booknara.booknaraPrj.admin.users.UserService;
import com.booknara.booknaraPrj.admin.users.Users;
import com.booknara.booknaraPrj.notification.dto.NotificationEntity;
import com.booknara.booknaraPrj.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final UserService userService;
    private final AdminSettingsService adminSettingsService;
    private final AdminInquiryService adminInquiryService;
    private final AdminBookManageMentService adminBookManagementService;
    private final AdminReportService adminReportService;
    private final AdminCombinedSupportService adminCombinedSupportService;
    private final AdminEventService adminEventService;
    private final AdminGenreService adminGenreService;
    private final StatisticsService statisticsService;
    private final AdminNotificationService adminNotificationService;
    private final NotificationService notificationService;


    @Value("${uploads.path}")
    private String uploadDir;

    @GetMapping("/BookManageMent")
    public String bookManagement(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "bookState", required = false) String bookState,
            Model model) {

        // [수정] PageRequest 생성 (한 페이지당 10개 혹은 100개 등 설정)
        Pageable pageable = PageRequest.of(page, 100);

        // [수정] 서비스 호출 시 인자 순서와 타입을 서비스 인터페이스와 맞춥니다.
        Slice<AdminBookListResponseDto> bookSlice = adminBookManagementService.getBookList(bookState, keyword, pageable);

        model.addAttribute("genreList", adminGenreService.getAllGenres());
        model.addAttribute("bookList", bookSlice.getContent());

        // 필터 및 검색어 유지
        model.addAttribute("filterState", (bookState != null) ? bookState : "");
        model.addAttribute("keyword", (keyword != null) ? keyword : "");
        model.addAttribute("currentPage", page);

        model.addAttribute("hasPrev", bookSlice.hasPrevious());
        model.addAttribute("hasNext", bookSlice.hasNext());

        return "admin/BookManageMent";
    }

    @PostMapping("/UpdateBookStatus")
    public String updateBookStatus(
            @RequestParam("bookId") Long bookId,
            @RequestParam("bookState") String bookState,
            RedirectAttributes redirectAttributes) {

        try {
            adminBookManagementService.updateStatus(bookId, bookState);
            redirectAttributes.addFlashAttribute("message", "도서 상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "상태 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/BookManageMent";
    }

    @PostMapping("/BookSave")
    public String saveBook(@ModelAttribute AdminBookSaveRequestDto dto, RedirectAttributes redirectAttributes) {
        try {
            // 서비스 호출하여 ISBN 정보와 실물 도서 정보 동시 저장
            adminBookManagementService.saveBookWithGenre(dto);

            // 성공 메시지 (HTML에서 alert 등으로 띄울 수 있음)
            redirectAttributes.addFlashAttribute("message", "새로운 도서가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            // 실패 시 에러 메시지 전달
            redirectAttributes.addFlashAttribute("error", "도서 등록에 실패했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/BookManageMent";
    }

    @GetMapping("/UserManageMent")
    public String UserManageMent(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page, // 페이지 파라미터 추가
            @RequestParam(value = "state", required = false) String state,
            Model model) {

        // 1. 유저 데이터 가져오기 (50개씩 페이징 처리)
        Page<Users> userPage = userService.getPagedUsers(keyword,state, page);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("keyword", keyword); // ⭐ 검색어 유지 (페이지 이동 시 필요)
        model.addAttribute("state", state);     // 현재 적용된 필터 상태

        // 2. 통계 데이터 가져오기
        Map<String, Long> stats = userService.getUserStatistics();
        model.addAttribute("stats", stats);

        // 저장된 설정에 따른 '회원관리' 메뉴 반환
        return "admin/UserManageMent";
    }
    @PostMapping("/UpdateUserStatus")
    public String updateUserStatus(@RequestParam String userId, @RequestParam String userState) {
        // 1. 서비스 호출하여 유저 상태 업데이트
        userService.updateUserState(userId, userState);

        // 2. 다시 회원관리 목록으로 리다이렉트
        return "redirect:/admin/UserManageMent";
    }

    @PostMapping("/UpdateUserNickname")
    public String updateUserNickname(@RequestParam("userId") String userId,
                                     @RequestParam("userNm") String userNm,
                                     RedirectAttributes redirectAttributes) {
        try {
            // 서비스 계층을 통해 DB 업데이트 실행
            userService.updateNickname(userId, userNm);

            // 성공 메시지 전달 (선택 사항)
            redirectAttributes.addFlashAttribute("message", "닉네임이 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "변경 중 오류가 발생했습니다.");
        }

        // 다시 회원관리 목록 페이지로 리다이렉트
        return "redirect:/admin/UserManageMent";
    }

    @GetMapping("/Statistics")
    public String statisticsPage(Model model){

        // 서비스로부터 실시간 통계 데이터 확보
        Map<String, Object> stats = statisticsService.getRealTimeUserStatistics();
        Map<String, Object> genderAgeStats = statisticsService.getGenderAgeStatistics();
        Map<String, Object> dashboardData = statisticsService.getDashboardData();

        // 1. 차트 관련 데이터
        model.addAttribute("ageStats", stats.get("ageStats"));
        model.addAttribute("genderStats", stats.get("genderStats"));
        model.addAttribute("maleAgeStats", genderAgeStats.get("maleAgeStats"));
        model.addAttribute("femaleAgeStats", genderAgeStats.get("femaleAgeStats"));
        model.addAttribute("monthlyStats", dashboardData.get("monthlyStats"));
        model.addAttribute("maxCount", dashboardData.get("maxCount"));

        // 2. 상단 카드 4종 지표 (HTML 변수명과 1:1 매핑)
        model.addAttribute("overdueRate", dashboardData.get("overdueRate"));      // 추가됨!
        model.addAttribute("extensionRate", dashboardData.get("extensionRate"));
        model.addAttribute("avgLendDays", dashboardData.get("avgLendDays"));
        model.addAttribute("avgOverdueDays", dashboardData.get("avgOverdueDays")); // 추가됨!

        // (참고) 기존에 쓰던 변수들도 필요하다면 유지
        model.addAttribute("currentLendCount", dashboardData.get("currentLendCount"));
        model.addAttribute("overdueCount", dashboardData.get("overdueCount"));

        return "admin/Statistics";
    }

    @GetMapping("/Settings")
    public String settingsPage(Model model) {
        // 1. DB에서 설정값을 가져옴
        AdminSettings settings = adminSettingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("activeBanners", adminEventService.getActiveBanners());
        // 2. "settings"라는 이름으로 모델에 담음 (HTML의 settings.xxx와 매칭됨)
        List<AdminEvent> allEvents = adminEventService.getAllActiveEvents();
        model.addAttribute("allEvents", allEvents);

        List<AdminBookSearchResponseDto> activeRecommends = adminSettingsService.getActiveRecommendations();
        model.addAttribute("activeRecommends", activeRecommends); // HTML의 th:each와 매칭

        model.addAttribute("activeBanners", adminEventService.getActiveBanners());
        model.addAttribute("allEvents", adminEventService.getAllActiveEvents());

        return "admin/Settings";
    }

    @PostMapping("/updateSettings")
    public String updateSettings(@ModelAttribute AdminSettings settings, RedirectAttributes redirectAttributes) {
        try {
            adminSettingsService.updateSettings(settings);
            // 저장 성공 메시지 전달
            redirectAttributes.addFlashAttribute("message", "설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/Settings";
    }

    @PostMapping("/updateBannerStatus")
    public String updateBanner(@RequestParam(required = false) List<Long> selectedEventIds) {
        adminEventService.updateMainBanners(selectedEventIds);
        return "redirect:/admin/Settings";
    }

    // 예: 검색 API (GET /api/books/search)
    @GetMapping("/search")
    @ResponseBody
    public Page<AdminBookSearchResponseDto> search(@RequestParam String keyword, Pageable pageable) {
        // Service에서 Entity를 조회한 뒤 DTO로 변환하여 리턴
        // JSON 결과: {"content": [{"isbn13": "123...", "title": "도서명", "author": "저자"}, ...], "totalPages": 10}
        return adminSettingsService.searchBooks(keyword, pageable);
    }

    // 예: 저장 API (POST /api/books/updateRecommendations)
    @PostMapping("/updateRecommendations")
    public ResponseEntity<?> update(@RequestBody AdminRecomSaveRequestDto request) {
        // JSON 입력: {"isbns": ["97889...", "97911..."]}
        adminSettingsService.updateRecommendations(request.getIsbns());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivateRecommendation")
    @ResponseBody
    public ResponseEntity<?> deactivate(@RequestBody Map<String, String> payload) {
        String isbn = payload.get("isbn");
        adminSettingsService.deactivateRecommendation(isbn);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/random")
    @ResponseBody
    public List<AdminBookSearchResponseDto> getRandomBooks(@RequestParam int count) {
        // 추천 도서를 랜덤으로 가져오는 서비스 로직
        return adminSettingsService.getRandomBooks(count);
    }

    @PostMapping("/admin/recom/save-random")
    @ResponseBody
    public ResponseEntity<String> saveRandomRecomBooks(@RequestBody List<String> isbns) {
        try {
            adminSettingsService.saveRandomRecomBooks(isbns);
            return ResponseEntity.ok("추천 도서가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/Inquiries")
    public String inquiryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "type", defaultValue = "ALL") String type,
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(defaultValue = "regDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // 1. 데이터 리스트 조회 (Pageable 설정)
        Pageable pageable = PageRequest.of(page, 10);
        Page<AdminCombinedSupport> combinedPage = adminCombinedSupportService.getFilteredList(
                keyword, type, status, sortField, sortDir, pageable);

        // 2. 리스트 데이터 및 페이징 정보 전달 (null 방지)
        if (combinedPage != null) {
            model.addAttribute("combinedList", combinedPage.getContent());
            model.addAttribute("totalPages", combinedPage.getTotalPages());
            model.addAttribute("totalElements", combinedPage.getTotalElements());
        } else {
            model.addAttribute("combinedList", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0);
        }

        // 3. 필터 및 정렬 상태 유지
        model.addAttribute("currentPage", page);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentType", type);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);

        // 4. 상단 카드 데이터 조회 (Long 타입 null 체크 및 0 처리 핵심)
        Long inquiryCount = adminInquiryService.getTotalCount();
        Long reportCount = adminReportService.getTotalCount();
        Long pendingCount = adminCombinedSupportService.getCountByState("N");
        Long resolvedCount = adminCombinedSupportService.getCountByState("Y");

        model.addAttribute("totalInquiryCount", inquiryCount != null ? inquiryCount : 0L);
        model.addAttribute("totalReportCount", reportCount != null ? reportCount : 0L);
        model.addAttribute("totalPendingCount", pendingCount != null ? pendingCount : 0L);
        model.addAttribute("totalResolvedCount", resolvedCount != null ? resolvedCount : 0L);

        return "admin/Inquiries";
    }

    @PostMapping("/Inquiries/answer")
    public String saveInquiryAnswer(@RequestParam("id") String inqId,
                                    @RequestParam("content") String respContent) {
        AdminInquiry adminInquiry = adminInquiryService.getInquiry(inqId);
        adminInquiry.setRespContent(respContent);
        adminInquiry.setRespState("Y");
        adminInquiry.setRespAt(LocalDateTime.now());
        adminInquiry.setRespUserId("admin");

        adminInquiryService.save(adminInquiry);

        // 문의 답장 알림 저장
        NotificationEntity notiEntity = new NotificationEntity();
        notiEntity.setUserId(adminInquiry.getUser().getUserId());
        notiEntity.setTargetType("INQUIRY_ANSWERED");
        notiEntity.setTargetId(inqId);
        notiEntity.setNotiContent("문의 답변이 완료되었습니다.");
        notiEntity.setCheckYn('N');
        notificationService.saveNotification(notiEntity);
        return "redirect:/admin/Inquiries";
    }

    // 신고 처리 완료 (새로 추가해야 할 부분)
    @PostMapping("/Reports/resolve")
    public String resolveReport(@RequestParam("id") String reportId,
                                @RequestParam("content") String resolveContent) {
        // 신고(Report) 엔티티를 조회하여 업데이트하는 로직을 작성하세요.
        // 예시:
        AdminReport adminReport = adminReportService.getReport(reportId);
        adminReport.setResolvedContent(resolveContent); // 신고 처리 내용 필드
        adminReport.setAdminReportState(AdminReportState.RESOLVED);
        adminReport.setResolvedAt(LocalDateTime.now());

        adminReportService.save(adminReport);
        return "redirect:/admin/Inquiries";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            Path path = Paths.get(uploadDir).resolve(decodedFileName).normalize();
            File file = path.toFile();

            if (!file.exists()) {
                System.out.println("파일을 찾을 수 없습니다: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String encodedFileName = UriUtils.encode(file.getName(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length()) // 파일 크기 명시 (중요)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/Notifications")
    public String notificationPage(
            @RequestParam(value = "type", required = false, defaultValue = "전체") String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        // 1. 서비스 호출 (DTO 기반 페이징 데이터 조회)
        // 이제 필터링과 검색이 서비스의 한 메서드 내에서 통합 처리됩니다.
        Page<AdminNotiResponseDto> notifications = adminNotificationService.getDetailedNotifications(type, keyword, pageable);

        // 2. 뷰에 데이터 전달
        model.addAttribute("notifications", notifications.getContent()); // 실제 리스트 데이터
        model.addAttribute("page", notifications);                     // 페이징 객체 (화면 하단 페이지 네이션용)
        model.addAttribute("selectedType", type);
        model.addAttribute("keyword", keyword);

        // 3. 왼쪽 메뉴(Support & Service) 배지용: 읽지 않은 알림 총 개수
        model.addAttribute("unreadCount", adminNotificationService.getUnreadCount());

        return "admin/Notifications";
    }

    /**
     * 알림 상세 확인 (모달에서 '확인 완료' 클릭 시 호출되는 API)
     * 비동기 처리를 위해 @ResponseBody 사용
     */
    @PostMapping("/Notifications/read/{notiId}")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Long notiId) {
        try {
            adminNotificationService.markAsRead(notiId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("fail");
        }
    }

    @PostMapping("/Notifications/readAll")
    @ResponseBody
    public ResponseEntity<String> readAllNotifications() {
        try {
            adminNotificationService.markAllAsRead(); // 위에서 만든 서비스 메서드 호출
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    // 문의용 변환기
    private AdminCombinedItemDto convertInquiryToDto(AdminInquiry inq) {
        AdminCombinedItemDto dto = new AdminCombinedItemDto();
        dto.setId(inq.getInqId());
        dto.setType("INQUIRY");
        dto.setSubType(inq.getInqTypeName());
        dto.setTitle(inq.getInqTitle());
        dto.setUserId(inq.getUser() != null ? inq.getUser().getUserId() : "익명");
        dto.setState(inq.getRespState());
        dto.setDate(inq.getCreatedAt());
        dto.setContent(inq.getInqContent());
        dto.setAnswer(inq.getRespContent());
        return dto;
    }

    // 신고용 변환기
    private AdminCombinedItemDto convertReportToDto(AdminReport rpt) {
        AdminCombinedItemDto dto = new AdminCombinedItemDto();
        dto.setId(rpt.getReportId());
        dto.setType("REPORT");

        // null 체크를 포함한 안전한 변환
        if (rpt.getAdminReportType() != null) {
            dto.setSubType(rpt.getAdminReportType().name());
        } else {
            dto.setSubType("기타");
        }

        dto.setTitle("콘텐츠 신고: " + (rpt.getAdminReportType() != null ? rpt.getAdminReportType().name() : "알 수 없음"));
        dto.setUserId(rpt.getUserId());

        // 상태 변환 (이미지 하단에 보이던 부분)
        if (rpt.getAdminReportState() != null) {
            dto.setState(rpt.getAdminReportState().name().equals("RESOLVED") ? "Y" : "N");
        } else {
            dto.setState("N");
        }

        dto.setDate(rpt.getReportedAt());
        dto.setContent(rpt.getReportContent());
        dto.setAnswer(null);
        return dto;
    }
}
