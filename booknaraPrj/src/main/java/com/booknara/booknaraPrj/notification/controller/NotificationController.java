package com.booknara.booknaraPrj.notification.controller;

import com.booknara.booknaraPrj.notification.dto.NotificationDTO;
import com.booknara.booknaraPrj.notification.dto.NotificationQueryParam;
import com.booknara.booknaraPrj.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    // 로그인시 풀링
    @ResponseBody
    @GetMapping("/notifications/unread/count")
    public Map<String, Integer> unreadCount(Authentication auth) {
        String userId = auth.getName();
        System.out.println("userId : " + userId);
        int count = service.findNewNotification(userId);

        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);

        return result;
    }

    // 알림 탭 클릭시 알림 가져오기
    @ResponseBody
    @GetMapping("/notification")
    public List<NotificationDTO> getNoti(NotificationQueryParam nqp,
                                         @RequestParam String tab,
                                         @RequestParam int page,
                                         @RequestParam int size,
                                         Authentication auth) {
        String userId = auth.getName();
        return service.findNotifications(nqp, userId, tab, page - 1, size);
    }

    // 알림 탭 클릭시 전체 행 가져오기
    @ResponseBody
    @GetMapping("/notification/totalRaw")
    public int getTotalRaw(@RequestParam String tab,
                           @RequestParam(required = false) Character checkYn,
                           Authentication auth) {
        System.out.println(checkYn);
        String userId = auth.getName();
        return service.notiCnt(userId, tab, checkYn);
    }

    // 알림 클릭시 읽음 처리
    @PatchMapping("/notification/{notiId}/read")
    public ResponseEntity<Void> readOne(@PathVariable long notiId,
                                        Authentication auth) {
        String userId = auth.getName();
        service.notificationRead(userId, notiId);
        return ResponseEntity.ok().build();
    }

    // 알림 전체 읽음 처리
    @PatchMapping("/notification/read-all/{tab}")
    public ResponseEntity<Void> readAll(@PathVariable String tab,
                                        Authentication auth) {
        String userId = auth.getName();
        service.notificationReadAll(userId, tab);
        return ResponseEntity.ok().build();
    }

    // 알림 전체 보기 뷰어
    @GetMapping("/notification/list")
    public String list() {
        return "notification/notification_list";
    }
}
