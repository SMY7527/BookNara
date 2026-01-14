package com.booknara.booknaraPrj.mypage.myinquiry;

import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/inquiry")
public class MyInquiryController {

    private final MyInquiryService inquiryService;

    /* ===============================
       내 문의 내역
    =============================== */
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

    /* ===============================
       문의 작성 화면
    =============================== */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("content", "mypage/InquiryWrite");
        return "mypage/mypagelayout";
    }

    /* ===============================
       문의 저장 (파일 최대 3개)
    =============================== */
    @PostMapping
    public String writeInquiry(
            MyInquiryWriteDto dto,
            @RequestParam(required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) return "redirect:/login";

        dto.setUserId(userDetails.getUserId());

        // ✅ 파일 최대 3개 제한
        if (files != null && files.size() > 3) {
            throw new IllegalArgumentException("파일은 최대 3개까지 업로드할 수 있습니다.");
        }

        inquiryService.writeInquiry(dto, files);

        return "redirect:my/inquiry";
    }
}
