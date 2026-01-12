package com.booknara.booknaraPrj.mypage;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.service.UserService1;
import com.booknara.booknaraPrj.mypage.mylibrary.MyLibraryService;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UserService1 userService1;
    private final MyLibraryService myLibraryService;

    @ModelAttribute
    public void addMyPageSidebarAttributes(Model model) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // 로그인 안 된 경우 → 사이드바 데이터 주입 안 함
        if (auth == null || !auth.isAuthenticated()) return;
        if ("anonymousUser".equals(auth.getPrincipal())) return;

        CustomUserDetails principal =
                (CustomUserDetails) auth.getPrincipal();

        String userId = principal.getUserId();

        /* ===============================
           1️⃣ 프로필 정보 (HTML 기준)
           =============================== */

        // 닉네임
        model.addAttribute("profileNm", principal.getProfileNm());

        // 프로필 이미지
        String profileImg;
        if (principal.getUseImg() == 0) {
            profileImg = "/img/mallang_default.png";
        } else {
            String dbImg = principal.getProfileImg();
            profileImg = (dbImg == null || dbImg.isBlank())
                    ? "/img/default_profile.png"
                    : dbImg;
        }
        model.addAttribute("profileImg", profileImg);

        /* ===============================
           2️⃣ 반납 / 연체 상태 메시지
           (기존 MyPageController 로직 재사용)
           =============================== */

        var status = myLibraryService.buildRentalStatusByDb(userId);

        model.addAttribute("statusText", status.get("statusText"));
        model.addAttribute("statusValue", status.get("statusValue"));
        model.addAttribute("statusLevel", status.get("statusLevel"));

        /* ===============================
           디버그 로그 (확인용)
           =============================== */
        System.out.println("🔥 GlobalModelAdvice(sidebar) userId=" + userId);
    }
}
