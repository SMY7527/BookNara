package com.booknara.booknaraPrj;

import com.booknara.booknaraPrj.admin.users.UserService;
import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.service.UserService1;
import com.booknara.booknaraPrj.mypage.mylibrary.MyLendDto;
import com.booknara.booknaraPrj.mypage.mylibrary.MyLibraryService;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MyPageController {

    private final UserService1 userService1;
    private final MyLibraryService myLibraryService;

    public MyPageController(UserService1 userService1, MyLibraryService myLibraryService) {
        this.userService1 = userService1;
        this.myLibraryService = myLibraryService;
    }

    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        if (principal == null) return "redirect:/login";

        String userId = principal.getUserId();

        // 1) 유저 기본 정보
        User user = userService1.findByUserId(userId);
        model.addAttribute("user", user);

        // 2) 프로필
        model.addAttribute("profileNm", principal.getProfileNm());

        String profileImg;
        if (principal.getUseImg() == 0) {
            profileImg = "/img/mallang_default.png";
        } else {
            String dbImg = principal.getProfileImg();
            profileImg = (dbImg == null || dbImg.isBlank()) ? "/img/default_profile.png" : dbImg;
        }
        model.addAttribute("profileImg", profileImg);

        // 3) 대여중(정상) 4개 + 연체 목록 (DB 쿼리 사용 버전)
        List<MyLendDto> currentRentals = myLibraryService.getCurrentNonOverdueLends(userId, 4);
        List<MyLendDto> overdueRentals = myLibraryService.getOverdueLendsSafe(userId);

        model.addAttribute("currentRentals", currentRentals);
        model.addAttribute("overdueRentals", overdueRentals);

        // 4) 상태 메시지 (D-2 / D+N) => 서비스에서만 계산
        Map<String, String> status = myLibraryService.buildRentalStatusByDb(userId);

        model.addAttribute("statusText", status.get("statusText"));
        model.addAttribute("statusValue", status.get("statusValue"));
        model.addAttribute("statusLevel", status.get("statusLevel"));

        // ✅ 핵심: 우측 조각 지정
        model.addAttribute("content", "mypage/mypage");


        // ✅ 핵심: 레이아웃 리턴
        return "mypage/mypagelayout";
    }

    // (옵션) FullCalendar 이벤트 API
    @GetMapping("/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> calendarEvents(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) return List.of();
        return myLibraryService.getCalendarEvents(principal.getUserId());
    }

}
