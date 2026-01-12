document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');
    if (!calendarEl) return;

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'ko',
        height: 350,

        headerToolbar: {
            left: 'prev,next',
            center: 'title',
            right: ''
        },

        // ✅ 기간 이벤트(대여기간 막대) 가독성
        eventDisplay: 'block',

        // ✅ DB에서 이벤트 받아오기
        events: '/my/calendar/events',

        eventClick: function (info) {
            const props = info.event.extendedProps || {};

            // ✅ 서버가 내려준 props 우선, 없으면 기본값
            const bookTitle = props.bookTitle || info.event.title || '(제목 없음)';

            // returnDueDate는 서버가 내려줄 수도 있고, 없으면 end/start로 대체
            // FullCalendar end는 미포함이라 end가 있으면 -1일이 실제 마지막일
            let returnDueDate = props.returnDueDate || '';

            if (!returnDueDate) {
                if (info.event.end) {
                    const end = new Date(info.event.end);
                    end.setDate(end.getDate() - 1); // 미포함 보정
                    returnDueDate = end.toISOString().slice(0, 10);
                } else if (info.event.start) {
                    returnDueDate = info.event.startStr || '';
                }
            }

            const overDue = props.overDue || 'N';
            const type = props.type || ''; // ACTIVE / OVERDUE / RETURNED 같은 값(서버가 주면)

            alert(
                `도서: ${bookTitle}\n반납예정: ${returnDueDate}\n연체: ${overDue}\n구분: ${type}`
            );

            // (옵션) 내서재로 이동하면서 강조
            // if (props.lendId) location.href = `/my/booklist?lendId=${props.lendId}`;
        }
    });

    calendar.render();
});
