package com.booknara.booknaraPrj.mainpage.service;

import com.booknara.booknaraPrj.mainpage.dto.EventBannerDTO;
import com.booknara.booknaraPrj.mainpage.mapper.MainEventBannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainEventBannerService {

    private final MainEventBannerMapper mapper;


    public List<EventBannerDTO> getBanners() {
        return mapper.findActiveOrUpcomingEvents();
    }
}
