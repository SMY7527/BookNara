package com.booknara.booknaraPrj.ebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/testView")
    public String testView(){
        return "ebook/test";
    }
}
