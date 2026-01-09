package com.booknara.booknaraPrj.admin;

import com.booknara.booknaraPrj.admin.bookMangement.BookListResponseDto;
import com.booknara.booknaraPrj.admin.bookMangement.BookManageMentService;
import com.booknara.booknaraPrj.admin.inquiry.Inquiry;
import com.booknara.booknaraPrj.admin.inquiry.InquiryService;
import com.booknara.booknaraPrj.admin.settings.Settings;
import com.booknara.booknaraPrj.admin.settings.SettingsService;
import com.booknara.booknaraPrj.admin.users.UserService;
import com.booknara.booknaraPrj.admin.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final UserService userService;
    private final SettingsService settingsService;
    private final InquiryService inquiryService;
    private final BookManageMentService bookManagementService;


    @GetMapping("/BookManageMent")
    public String bookManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            Model model) {

        Page<BookListResponseDto> bookPage = bookManagementService.getBookList(page, keyword);

        int totalPages = Math.max(1, bookPage.getTotalPages());
        int pageBlock = 5;
        int currentBlock = page / pageBlock; // 0: 1~5페이지, 1: 6~10페이지...

        int startPage = (currentBlock * pageBlock) + 1;
        int endPage = Math.min(startPage + pageBlock - 1, totalPages);

        model.addAttribute("bookList", bookPage.getContent());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // [수정된 로직] 이전 블록이 있으면 true
        model.addAttribute("hasPrev", startPage > 1);
        // [수정된 로직] 현재 블록의 끝이 전체 페이지보다 작으면 다음 블록이 있음
        model.addAttribute("hasNext", endPage < totalPages);

        return "admin/BookManageMent";
    }

    @GetMapping("/UserManageMent")
    public String UserManageMent(@RequestParam(value = "keyword", required = false) String keyword,
                           Model model) {

        // 1. 유저 데이터 가져오기 (검색어가 있으면 검색, 없으면 전체)
        List<Users> userList = userService.searchUsers(keyword);
        model.addAttribute("users", userList);

        // 2. 통계 데이터 가져오기
        Map<String, Long> stats = userService.getUserStatistics();
        model.addAttribute("stats", stats);

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
    public String adminBook9(){
        return "admin/Statistics";
    }

    @GetMapping("/Settings")
    public String settingsPage(Model model) {
        // 1. DB에서 설정값을 가져옴
        Settings settings = settingsService.getSettings();

        // 2. "settings"라는 이름으로 모델에 담음 (HTML의 settings.xxx와 매칭됨)
        model.addAttribute("settings", settings);

        return "admin/Settings";
    }

    @PostMapping("/updateSettings")
    public String updateSettings(@ModelAttribute Settings settings, RedirectAttributes redirectAttributes) {
        try {
            settingsService.updateSettings(settings);
            // 저장 성공 메시지 전달
            redirectAttributes.addFlashAttribute("message", "설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/Settings";
    }

    @GetMapping("/Inquiries")
    public String inquiryList(@RequestParam(value = "filter", required = false) String filter, Model model) {
        List<Inquiry> inquiries = inquiryService.getFilteredInquiries(filter);
        model.addAttribute("inquiries", inquiries); // HTML에서 'inquiries'라는 이름으로 사용

        Map<String, Long> stats = inquiryService.getInquiryStats();
        model.addAttribute("stats", stats);

        // 3. 현재 어떤 필터가 적용되었는지 뷰에 전달 (UI 강조용)
        model.addAttribute("currentFilter", filter);
        return "admin/Inquiries"; // resources/templates/admin/inquiryList.html
    }

    @PostMapping("/Inquiries/answer")
    public String saveAnswer(@RequestParam String inqId, @RequestParam String respContent) {
        // 1. DB에서 해당 문의 조회
        Inquiry inquiry = inquiryService.getInquiry(inqId);

        // 2. 데이터 업데이트
        inquiry.setRespContent(respContent);
        inquiry.setRespState("Y");
        inquiry.setRespAt(LocalDateTime.now());
        inquiry.setRespUserId("admin"); // 관리자 계정

        // 3. 저장 및 리다이렉트
        inquiryService.save(inquiry);

        return "redirect:/admin/Inquiries";
    }

    @GetMapping("/Notifications")
    public String adminBook12(){
        return "admin/Notifications";
    }
}
