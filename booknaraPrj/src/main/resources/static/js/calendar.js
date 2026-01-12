document.addEventListener("DOMContentLoaded", function () {
    const calendarEl = document.getElementById("calendar");
    if (!calendarEl) return;

    // 날짜 yyyy-MM-dd
    function toDateStr(v){
        if (!v) return "";
        if (typeof v === "string") return v.substring(0, 10);
        const d = new Date(v);
        const y = d.getFullYear();
        const m = String(d.getMonth()+1).padStart(2,"0");
        const da = String(d.getDate()).padStart(2,"0");
        return `${y}-${m}-${da}`;
    }

    function addDays(dateStr, days){
        const d = new Date(dateStr + "T00:00:00");
        d.setDate(d.getDate()+days);
        return toDateStr(d);
    }

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        locale: "ko",
        height: 350,

        headerToolbar: { left: "prev,next", center: "title", right: "" },

        // ✅ 핵심: 서버 이벤트 그대로 사용 (내서재 연동 유지)
        events: "/my/calendar/events",

        // ✅ 기간 이벤트가 막대로 보이게
        eventDisplay: "block",

        // ✅ 서버 이벤트를 "막대 + hover 확장 텍스트" 형태로 직접 렌더링
        eventContent: function(arg){
            const p = arg.event.extendedProps || {};

            // 서버에서 bookTitle이 있으면 그걸 우선 사용, 없으면 title
            const bookTitle = p.bookTitle || arg.event.title || "(제목 없음)";

            // 서버가 lendDate/returnDueDate 내려주면 사용
            const lendDate = toDateStr(p.lendDate) || toDateStr(arg.event.start);
            let dueDate = toDateStr(p.returnDueDate);

            // FullCalendar end는 미포함이라 end가 있으면 -1일이 실제 마지막날.
            if (!dueDate) {
                if (arg.event.end) {
                    const end = new Date(arg.event.end);
                    end.setDate(end.getDate() - 1);
                    dueDate = toDateStr(end);
                } else {
                    dueDate = toDateStr(arg.event.start);
                }
            }

            const rangeText = (lendDate && dueDate) ? `${lendDate} ~ ${dueDate}` : "";

            // ✅ 막대 안 내용: 기본은 날짜만/혹은 빈값, hover 때 책 제목까지 보이게 CSS로 제어
            return {
                html: `
          <div class="lend-bar-inner">
            <span class="bar-date">${rangeText}</span>
            <span class="bar-title">${bookTitle}</span>
          </div>
        `
            };
        },

        // ✅ 상태별 클래스(연체/대여중/이력) 적용
        eventClassNames: function(arg){
            const p = arg.event.extendedProps || {};
            const cls = ["lend-bar"];

            // 1) 서버가 type을 주는 경우 (ACTIVE/OVERDUE/RETURNED)
            const type = (p.type || "").toUpperCase();
            if (type === "OVERDUE") cls.push("bar-overdue");
            else if (type === "RETURNED") cls.push("bar-returned");
            else cls.push("bar-active");

            // 2) overDue='Y'로 주는 경우도 연체 처리
            if ((p.overDue || p.overdue || "N") === "Y") {
                cls.push("bar-overdue");
                cls = cls.filter(x => x !== "bar-active"); // active 제거
            }

            return cls;
        },

        // ✅ 클릭 시 내서재로 이동(원하면 lendId 사용)
        eventClick: function(info){
            const p = info.event.extendedProps || {};
            // 예: lendId를 서버가 주면 해당 책 강조해서 내서재로 이동 가능
            // if (p.lendId) location.href = `/my/booklist?lendId=${p.lendId}`;
        }
    });

    calendar.render();
});
