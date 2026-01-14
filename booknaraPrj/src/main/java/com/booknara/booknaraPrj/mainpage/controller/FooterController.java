package com.booknara.booknaraPrj.mainpage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/terms")
    public String terms(Model model){
        model.addAttribute("effectiveDate", "2026년 1월 14일");
        return "terms";
    }


    @GetMapping("/copyright")
    public String copyright(Model model){
        model.addAttribute("updatedDate", "2026년 1월 14일");
        model.addAttribute("contactEmail", "support@booknara.com"); // 너네 메일로 바꾸기
        return "copyright"; // templates/copyright.html
    }
}
