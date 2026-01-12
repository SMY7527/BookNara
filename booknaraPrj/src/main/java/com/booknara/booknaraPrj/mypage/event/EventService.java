package com.booknara.booknaraPrj.mypage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;

    public List<EventHistoryDto> getMyEventHistory(String userId, String type) {
        return eventMapper.selectMyEventHistory(userId, type);
    }

}
