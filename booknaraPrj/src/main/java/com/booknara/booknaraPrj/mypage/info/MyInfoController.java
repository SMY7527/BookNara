package com.booknara.booknaraPrj.mypage.info;

import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/myinfo")
public class MyInfoController {

    private final MyInfoService myInfoService;

    @GetMapping
    public String myInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        String userId = userDetails.getUserId();

        MyInfoDto myInfo = myInfoService.getMyInfo(userId);

        if (myInfo == null) { // DB에 없을 때 대비
            myInfo = new MyInfoDto();
            myInfo.setUserId(userId);
        }

        model.addAttribute("myInfo", myInfo);
        model.addAttribute("loginUser", userDetails);


        return "mypage/myinfo";
    }

    @PostMapping("/update")
    public String updateMyInfo(MyInfoDto dto, @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        String userId = userDetails.getUserId();
        dto.setUserId(userId);

        myInfoService.updateMyInfo(dto);


        return "redirect:/mypage/myinfo";
    }
}
