package com.booknara.booknaraPrj.mypage.mylibrary;

import com.booknara.booknaraPrj.security.CustomUserDetails;import lombok.RequiredArgsConstructor;
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

        return "mypage/mybooklist";
    }

    @GetMapping("/reserve")
    public String myReserve(Model model) {
        model.addAttribute("activeTab", "reserve");
        return "mypage/myreserve";
    }

    @GetMapping("/bookmark")
    public String myBookmark(Model model) {
        model.addAttribute("activeTab", "bookmark");
        return "mypage/mybookmark";
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
