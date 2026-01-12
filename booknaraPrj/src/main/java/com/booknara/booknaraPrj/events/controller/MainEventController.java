package com.booknara.booknaraPrj.events.controller;

import com.booknara.booknaraPrj.events.dto.EventDTO;
import com.booknara.booknaraPrj.events.service.MainEventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class MainEventController {

    private final MainEventService eventService;

    public MainEventController(MainEventService eventService) {
        this.eventService = eventService;
    }


    @GetMapping("/event/list")
    public String getEventsList(
            @RequestParam(value = "tab", defaultValue = "ongoing") String tab,
            Model model
    ) {
        int soonDays = 7;
        List<EventDTO> list = eventService.getEventListByTab(tab, soonDays);


        System.out.println( "list^^" +  list );

        model.addAttribute("list", list);
        model.addAttribute("tab", tab);
        return "event/list";
    }
    
    
    
    
    
    
    @GetMapping("/event/detail")
    public  String  getEvent(  @RequestParam(value="id")  
                               String id   , Model model){
        
        
        //
        System.out.println( "id");
        //이벤트  id  = >1   가져오기
        
        EventDTO  eventDTO =  eventService.getEventDTOByID(id);

        System.out.println( eventDTO);

        model.addAttribute("event" ,eventDTO );
        return "event/detail";
        
        
        
    }






    //이벤트 등록하기
    // get
    // post

    @GetMapping("/event/register")
    public String registerEvent() {
        return "event/eventForm";
    }

    @PostMapping("/event/register")
    public String registerEventSubmit(EventDTO eventDTO,
                                      @RequestParam(value = "thumbFile", required = false) MultipartFile thumbFile) {
        eventService.createEvent(eventDTO, thumbFile); // DB 저장 + (있으면) 썸네일 저장
        return "redirect:/event/list";
    }






}
