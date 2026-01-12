package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyLibraryService {

    private final MyLibraryMapper myLibraryMapper;

    public List<MyLendDto> getCurrentLends(String userId) {
        List<MyLendDto> list = myLibraryMapper.selectCurrentLends(userId);
        return list == null ? List.of() : list;
    }

    // ✅ (추가) 마이페이지 “정상 대여중 4개”를 확실히 만들기
    public List<MyLendDto> getCurrentNonOverdueLends(String userId, int limit) {
        List<MyLendDto> lends = getCurrentLends(userId);

        return lends.stream()
                .filter(l -> l.getReturnDoneAt() == null)
                .filter(l -> !"Y".equalsIgnoreCase(l.getOverDue()))
                .sorted(Comparator.comparing(MyLendDto::getReturnDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ✅ (수정) null 방지해서 안전하게
    public List<MyLendDto> getOverdueLendsSafe(String userId) {
        List<MyLendDto> list = myLibraryMapper.selectOverdueLends(userId);
        return list == null ? List.of() : list;
    }

    public MyLendDto getNearestDueLend(String userId) {
        return myLibraryMapper.selectNearestDueLend(userId);
    }

    // ✅ (추천) DB 조회 기반으로 상태메시지 만들기 (연체/가까운 반납일만 써서 효율 좋음)
    public Map<String, String> buildRentalStatusByDb(String userId) {
        Map<String, String> result = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 1) 연체가 있으면 D+N
        List<MyLendDto> overdue = getOverdueLendsSafe(userId);
        if (!overdue.isEmpty()) {
            long maxOverdueDays = overdue.stream()
                    .map(MyLendDto::getReturnDueDate)
                    .filter(Objects::nonNull)
                    .map(due -> ChronoUnit.DAYS.between(due.toLocalDate(), today))
                    .max(Long::compareTo)
                    .orElse(1L);

            result.put("statusText", "대여 불가능");
            result.put("statusValue", "D+" + Math.max(1L, maxOverdueDays));
            result.put("statusLevel", "danger");
            return result;
        }

        // 2) 연체 없으면 가장 가까운 반납예정일로 D-n
        MyLendDto nearest = getNearestDueLend(userId);
        if (nearest == null || nearest.getReturnDueDate() == null) {
            result.put("statusText", "대여중인 도서 없음");
            result.put("statusValue", "");
            result.put("statusLevel", "none");
            return result;
        }

        long days = ChronoUnit.DAYS.between(today, nearest.getReturnDueDate().toLocalDate());
        result.put("statusText", "반납 ");
        result.put("statusValue", "D-" + Math.max(0L, days));
        result.put("statusLevel", (days <= 2 ? "danger" : "warn"));
        return result;
    }

    public Map<LocalDate, List<MyLendDto>> getLendHistoryGroups(String userId) {
        List<MyLendDto> history = myLibraryMapper.selectLendHistory(userId);
        if (history == null || history.isEmpty()) return Collections.emptyMap();

        // 반납 완료된 것만 날짜별 그룹핑
        return history.stream()
                .filter(h -> h.getReturnDoneAt() != null)
                // 최신 날짜가 위로 오게
                .sorted(Comparator.comparing(MyLendDto::getReturnDoneAt).reversed())
                .collect(Collectors.groupingBy(
                        h -> h.getReturnDoneAt().toLocalDate(), // ✅ returnDoneAt이 LocalDateTime일 때
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
    public List<Map<String, Object>> getCalendarEvents(String userId) {
        List<MyLendDto> lends = myLibraryMapper.selectCalendarLends(userId);
        if (lends == null) lends = List.of();

        List<Map<String, Object>> events = new ArrayList<>();

        for (MyLendDto l : lends) {
            // FullCalendar는 YYYY-MM-DD 형태의 문자열이면 OK
            String dueDate = (l.getReturnDueDate() == null)
                    ? null
                    : l.getReturnDueDate().toLocalDate().toString();

            if (dueDate == null) continue;

            boolean overdue = "Y".equalsIgnoreCase(l.getOverDue());

            Map<String, Object> e = new HashMap<>();
            e.put("id", String.valueOf(l.getLendId()));
            e.put("title", overdue
                    ? "[연체] " + l.getBookTitle()
                    : "[반납예정] " + l.getBookTitle());
            e.put("start", dueDate);          // 하루짜리 이벤트
            e.put("allDay", true);

            // 색상(원하면 CSS로 통일 가능)
            e.put("color", overdue ? "#ff6b6b" : "#4dabf7");

            // 클릭했을 때 상세에 쓸 데이터
            Map<String, Object> ext = new HashMap<>();
            ext.put("lendId", l.getLendId());
            ext.put("bookTitle", l.getBookTitle());
            ext.put("overDue", l.getOverDue());
            ext.put("returnDueDate", dueDate);
            e.put("extendedProps", ext);

            events.add(e);
        }

        return events;
    }


    public List<Map<String, Object>> buildCalendarPeriodEvents(String userId) {
        // 1) 반납 전(대여중 + 연체)
        List<MyLendDto> active = Optional.ofNullable(myLibraryMapper.selectCalendarActiveLends(userId))
                .orElseGet(List::of);

        // 2) 반납 완료(과거)
        List<MyLendDto> returned = Optional.ofNullable(myLibraryMapper.selectCalendarReturnedLends(userId))
                .orElseGet(List::of);

        List<Map<String, Object>> events = new ArrayList<>();

        // ✅ FullCalendar end는 '미포함'이라서, 보여주고 싶은 마지막날 +1일로 내려줌
        // 예) 1/1 ~ 1/10(반납예정일) 막대 표시 => end를 1/11로
        for (MyLendDto l : active) {
            if (l.getLendDate() == null) continue;

            LocalDate start = l.getLendDate().toLocalDate();
            LocalDate due = (l.getReturnDueDate() != null)
                    ? l.getReturnDueDate().toLocalDate()
                    : start;

            boolean isOverdue = "Y".equalsIgnoreCase(l.getOverDue());

            Map<String, Object> e = new HashMap<>();
            e.put("title", l.getBookTitle());
            e.put("start", start.toString());
            e.put("end", due.plusDays(1).toString()); // ⭐️ 마지막 날 포함 표시
            e.put("color", isOverdue ? "#ff4d4f" : "#2f80ed"); // 빨강/파랑

            // 클릭했을 때 정보
            e.put("lendId", l.getLendId());
            e.put("overDue", l.getOverDue());
            e.put("type", isOverdue ? "OVERDUE" : "ACTIVE");

            events.add(e);
        }

        for (MyLendDto l : returned) {
            if (l.getLendDate() == null) continue;

            LocalDate start = l.getLendDate().toLocalDate();
            LocalDate done = (l.getReturnDoneAt() != null)
                    ? l.getReturnDoneAt().toLocalDate()
                    : start;

            Map<String, Object> e = new HashMap<>();
            e.put("title", l.getBookTitle());
            e.put("start", start.toString());
            e.put("end", done.plusDays(1).toString()); // ⭐️ 마지막 날 포함 표시
            e.put("color", "#9aa0a6"); // 회색

            e.put("lendId", l.getLendId());
            e.put("type", "RETURNED");

            events.add(e);
        }

        return events;
    }




    // ===== 반납 / 연장 =====
    public void returnBook(String lendId) {
        myLibraryMapper.updateReturnDone(lendId);
    }

    public void extendBook(String lendId) {
        myLibraryMapper.updateExtendLend(lendId);
    }
}
