package com.booknara.booknaraPrj.mainpage.controller;

import com.booknara.booknaraPrj.mainpage.dto.EventBannerDTO;
import com.booknara.booknaraPrj.mainpage.service.MainEventBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class EventBannerController {

    private final MainEventBannerService mainEventBannerService;

    @GetMapping("/events")
    public List<EventBannerDTO> getBanners() {
        return mainEventBannerService.getBanners();
    }
}
