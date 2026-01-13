package com.booknara.booknaraPrj.mainpage.mapper;

import com.booknara.booknaraPrj.mainpage.dto.EventBannerDTO;
import java.util.List;

public interface MainEventBannerMapper {

    List<EventBannerDTO> findActiveOrUpcomingEvents();

}
