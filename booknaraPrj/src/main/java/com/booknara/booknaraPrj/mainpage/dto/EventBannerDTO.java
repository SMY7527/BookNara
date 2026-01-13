package com.booknara.booknaraPrj.mainpage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBannerDTO {
    private Long eventId;
    private String eventTitle;
    private String imgUrl;
}
