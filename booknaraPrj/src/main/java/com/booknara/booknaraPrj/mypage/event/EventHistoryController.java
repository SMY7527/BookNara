package com.booknara.booknaraPrj.mypage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/events")
public class EventHistoryController {

    private final EventService eventService;

    @GetMapping
    public String myEventHistory(
            @RequestParam(defaultValue = "all") String type,
            HttpSession session,
            Model model
    ) {

        // ✅ 임시 로그인 유저 ID (DB에 실제 존재하는 값)
        String userId = "user01";   // ← EVENT_PARTICIPANTS.USER_ID에 있는 값

        List<EventHistoryDto> eventList =
                eventService.getMyEventHistory(userId, type);

        model.addAttribute("eventList", eventList);
        model.addAttribute("activeEvent", type);

        return "mypage/eventHistory";
    }
}
