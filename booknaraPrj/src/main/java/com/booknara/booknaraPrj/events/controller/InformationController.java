package com.booknara.booknaraPrj.events.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InformationController {

    @GetMapping("/information")
    public String infoHome(){
        return "information/information";


    }

    @GetMapping("/location")
    public String locationView(){
        return "information/location";
    }

    @GetMapping("/flow")
    public String flowView(){
        return "information/flow";
    }



    @GetMapping("/guide")
    public String guideView(){
        return "information/guide";
    }



    @GetMapping("/locker")
    public String lockerView(){
        return "information/locker";
    }
    @GetMapping("/schedule")
    public String scheduleView(){
        return "information/schedule";
    }



    @GetMapping("/guideDetail")    //guideDetail?id=1
    public String guideDetail(@RequestParam( name="id" ) String id     , Model model ){
        model.addAttribute("id", id);
        return "information/guideDetail";
    }


}
