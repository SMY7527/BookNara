package com.booknara.booknaraPrj.events.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FAQController {
    @GetMapping("/faq")
    public String faqView() {
        return "information/faq";
    }

    @GetMapping("/faq/search")
    public String faqSearch() {
        return "information/faq";
    }
}
