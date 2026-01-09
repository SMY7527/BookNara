package com.booknara.booknaraPrj.mypage.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventHistoryDto {

    private Long eventId;

    // 이벤트 테이블 쪽
    private String eventTitle;
    private String eventBanner;      // 배너 이미지 경로/URL (없으면 null)
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // 참여 테이블 쪽
    private String resultYn;         // 'Y' / 'N' (없으면 null 가능)

    // 화면용 상태값
    private String status;           // 진행중 / 종료 / 당첨
}
