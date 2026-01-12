package com.booknara.booknaraPrj.mypage.myinquiry;


import com.booknara.booknaraPrj.security.CustomUserDetails;import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/inquiry")
public class MyInquiryController {

    private final MyInquiryService inquiryService;

    // 내 문의 내역
    @GetMapping
    public String inquiryList(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUserId();

        model.addAttribute("inquiryList",
                inquiryService.getMyInquiry(userId, keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "mypage/InquiryHistory");
        return "mypage/mypagelayout";


    }

    // 문의 작성 화면
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("content", "mypage/InquiryWrite");
        return "mypage/mypagelayout";
    }


    // 문의 저장
    @PostMapping
    public String writeInquiry(MyInquiryWriteDto dto, @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {


        System.out.println(  "login" +  userDetails);
        dto.setUserId(userDetails.getUserId());
        inquiryService.writeInquiry(dto);
        return "redirect:/my/inquiry";
    }
}
