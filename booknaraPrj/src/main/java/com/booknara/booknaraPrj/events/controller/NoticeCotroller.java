package com.booknara.booknaraPrj.events.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticeCotroller {

    @GetMapping("/notice")
    public String noticeView() {
        return "information/notice";
    }

    @GetMapping("/noticeDetail")
    public String noticeDetailView() {
        return "information/noticeDetail";
    }
}
