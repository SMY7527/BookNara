package com.booknara.booknaraPrj.events.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InquiryController {

    @GetMapping("/inquiry")
    public String inquiryView() {
        return "/information/inquiry";
    }
}
