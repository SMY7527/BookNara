package com.booknara.booknaraPrj.mypage.mylibrary;

import com.booknara.booknaraPrj.security.CustomUserDetails;import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyBookListController {

    private final MyLibraryService myLibraryService;

    @GetMapping("/booklist")
    public String myBookList( @AuthenticationPrincipal CustomUserDetails userDetails,Model model) {
        String userId = userDetails.getUserId();

        model.addAttribute("activeTab", "rent");
        model.addAttribute("currentLends", myLibraryService.getCurrentLends(userId));
        model.addAttribute("historyGroups", myLibraryService.getLendHistoryGroups(userId));
        model.addAttribute("content", "mypage/mybooklist");
        return "mypage/mypagelayout";
    }

    @GetMapping("/reserve")
    public String myReserve(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        String userId = userDetails.getUserId();
        model.addAttribute("activeTab", "reserve");
        model.addAttribute("reserveList", myLibraryService.getMyReservations(userId));
        model.addAttribute("content", "mypage/myreserve");
        return "mypage/mypagelayout";
    }

    @PostMapping("/reserve/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelReserve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String rsvId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        boolean ok = myLibraryService.cancelReservation(userDetails.getUserId(), rsvId);
        if (!ok) {
            return ResponseEntity.ok(Map.of("success", false, "message", "예약 취소 실패"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }


    @GetMapping("/bookmark")
    public String myBookmark(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails == null) {
            // 로그인 안 된 상태면 로그인 페이지로 보내거나, 네 프로젝트 흐름에 맞게 처리
            return "redirect:/login";
        }

        String userId = userDetails.getUserId();

        model.addAttribute("activeTab", "bookmark");
        model.addAttribute("bookmarkList", myLibraryService.getMyBookmarks(userId)); // ✅ 여기 이름 맞추기
        model.addAttribute("content", "mypage/mybookmark");
        return "mypage/mypagelayout";
    }


    // ✅ 북마크 토글 (Y<->N). 북마크 탭에서 누르면 N이 되어 화면에서 제거하면 됨
    @PostMapping("/bookmark/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String isbn13
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        String next = myLibraryService.toggleBookmark(userDetails.getUserId(), isbn13);
        return ResponseEntity.ok(Map.of("success", true, "bookmarkYn", next));
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> calendarEvents(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return List.of();
        return myLibraryService.buildCalendarPeriodEvents(userDetails.getUserId());
    }


    /* ================= 반납 / 연장 ================= */

    @PostMapping("/lend/return")
    @ResponseBody
    public void returnBook(@RequestParam String lendId) {
        myLibraryService.returnBook(lendId);
    }

    @PostMapping("/lend/extend")
    @ResponseBody
    public void extendBook(@RequestParam String lendId) {
        myLibraryService.extendBook(lendId);
    }
}
