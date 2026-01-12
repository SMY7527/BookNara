/*
“이 로직은 알림 DOM이 항상 존재한다는 전제로 짜여있는데, 실제로는 비로그인/페이지별 헤더 구성 차이 때문에 null이 나올 수 있다.”
“그래서 특정 페이지에서 querySelector(...)가 null을 반환하고, 그 상태로 addEventListener를 걸면서 콘솔 에러가 난다.”
“해결은 로직 변경이 아니라, DOM 존재 여부 체크(null guard) 를 최소로 추가하면 된다.”
* */



document.addEventListener("DOMContentLoaded", () => {
  const alarmBtn = document.getElementById("alarmBtn");
  const alarmBox = document.getElementById("alarmBox");

  // ✅ [체크1] 비로그인/헤더 미포함 페이지에서는 alarmBtn/alarmBox가 null일 수 있음
  // -> 아래에서 addEventListener 걸기 전에 null 가드 필요
  if (alarmBtn && alarmBox) {
    alarmBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      alarmBox.style.display = alarmBox.style.display === "block" ? "none" : "block";

      const tab = document.querySelector(".tab.active");
      // ✅ [체크2] tab이 null이면 tab.dataset에서 터짐 (초기 active 탭이 없을 수도 있음)
      // -> tab 존재 여부 확인 필요
      const filter = tab.dataset.filter;
      const url = `/notification?tab=${filter}&page=1&size=10&checkYn=N`;
      loadTab(url);
    });

    alarmBox.addEventListener("click", (e) => {
      e.stopPropagation();  // 알람 내부 클릭으로 닫히기 않게
      closeAlarmMenu();
    })

    document.addEventListener("click", (e) => {
      alarmBox.style.display = "none";
      closeAlarmMenu();
    });
  }

  // 알림 메뉴
  const alarmMenuDot = document.querySelector(".alarm-menu-dot");
  const alarmMenu = document.querySelector(".alarm-menu");
  const readBtn = document.querySelector("#allread");
  const tabs = document.querySelectorAll(".tab");
  const allNotiBtn = document.querySelector("#all-noti-btn");

  // ✅ [체크3] window.IS_LOGGED_IN이 true여도 DOM이 항상 존재한다는 보장은 없음
  // (페이지마다 header 구성 차이, 권한/템플릿 분기 등)
  // -> alarmMenuDot/alarmMenu/readBtn/allNotiBtn null 가드 필요

  // 로그인이 되어 있다면
  if (window.IS_LOGGED_IN) {
    alarmMenuDot.addEventListener("click", (e) => {
      e.stopPropagation();
      alarmMenuDot.classList.toggle("alarm-menu-active");
      alarmMenu.style.display = alarmMenu.style.display === "flex" ? "none" : "flex";
    });

    alarmMenu.addEventListener("click", (e) => {
      e.stopPropagation();
    })

    readBtn.addEventListener("click", () => {
      // 전체 읽음 fetch
      const tab = document.querySelector(".tab.active");
      const filter = tab.dataset.filter;

      fetch(`/notification/read-all/${filter}`, {
        method: "PATCH"
      });
    });

    allNotiBtn.addEventListener("click", () => {
      window.location.href = "/notification/list";
    });

    tabs.forEach(tab => {
    tab.addEventListener("click", (e) => {
      e.stopPropagation(); // ⭐ 중요

      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const filter = tab.dataset.filter;
      let url = `/notification?tab=${filter}&page=1&size=10`;
      if(filter==='unread') url += '&checkYn=N';
      loadTab(url);
    });
  });

    startNotificationPolling();
  }

  function closeAlarmMenu() {
    // ✅ [체크4] closeAlarmMenu()는 여러 군데서 호출됨
    // -> alarmMenuDot / alarmMenu가 null일 때도 안전해야 함 (여기서도 터질 수 있음)
    alarmMenuDot.classList.remove("alarm-menu-active");
    alarmMenu.style.display = "none";
  }

  const alarmTabs = document.querySelector('.alarm-tabs');

  let isDown = false;
  let startX = 0;
  let startScrollLeft = 0;
  let moved = false;
  let pointerId = null;

  const MOVE_THRESHOLD = 10; // px: 이 이상 움직이면 '드래그'로 판단


  // ✅ [체크5] 비로그인(또는 헤더 알림 UI 없는 페이지)면 alarmTabs = null
  // -> 아래 pointer 이벤트 등록에서 addEventListener 하면서 터짐 (지금 에러의 유력 후보)

  alarmTabs.addEventListener('pointerdown', (e) => {
    // 왼쪽 버튼만 (마우스)
    if (e.pointerType === 'mouse' && e.button !== 0) return;

    isDown = true;
    moved = false;

    // alarmTabs.classList.add('dragging');
    // alarmTabs.setPointerCapture(e.pointerId);
    pointerId = e.pointerId;

    startX = e.clientX;
    startScrollLeft = alarmTabs.scrollLeft;
  });

  alarmTabs.addEventListener('pointermove', (e) => {
    if (!isDown) return;

    const dx = e.clientX - startX;

    // threshold 넘기 전까지는 클릭로 간주 (아무 것도 안함)
    if (!moved && Math.abs(dx) < MOVE_THRESHOLD) return;

    // 여기부터는 드래그 확정
    moved = true;
    alarmTabs.classList.add('dragging');
    alarmTabs.setPointerCapture(e.pointerId);
    e.preventDefault(); // 드래그 중 스크롤/선택 방지

    alarmTabs.scrollLeft = startScrollLeft - dx;
  });

  alarmTabs.addEventListener('pointerup', () => endDrag());
  alarmTabs.addEventListener('pointercancel', () => endDrag());
  alarmTabs.addEventListener('pointerleave', () => endDrag());

  function endDrag() {
    isDown = false;
    alarmTabs.classList.remove('dragging');
  }
});

// targetType 매핑 객체
// 링크 생성은 targetId가 어떤 의미인지에 따라 달라지니까 함수로 두는 게 좋음
const NOTI_META = {
  RENTAL_DUE: {
    tab: 'SYSTEM',
    icon: 'lend_expiration_noti',
    title: '반납 기한',
    href: () => '/my/library' // 내 서재
  },

  PAST_DUE: {
    tab: 'SYSTEM',
    icon: 'past_due_noti',
    title: '연체',
    href: () => '/my/library' // 내 서재
  },

  RESERVATION_AVAILABLE: {
    tab: 'SYSTEM',
    icon: 'resv_noti',
    title: '예약 도서',
    href: (n) => `/books/${n.targetId}` // 도서 상세 페이지
  },

  FEED_COMMENT: {
    tab: 'COMMUNITY',
    icon: 'feed_comment_noti',
    title: '댓글',
    href: (n) => `/feeds/${n.targetId}` // 피드 상세
  },

  FEED_LIKE: {
    tab: 'COMMUNITY',
    icon: 'feed_like_noti',
    title: '좋아요',
    href: (n) => `/feeds/${n.targetId}` // 피드 상세
  },

  EVENT_WON: {
    tab: 'EVENT',
    icon: 'event_noti',
    title: '이벤트',
    href: (n) => `/events/${n.targetId}` // 이벤트 상세
  },

  ACCOUNT_RESTRICTED: {
    tab: 'SYSTEM',
    icon: 'restricted_noti',
    title: '계정 제한',
    href: () => '#' // 밴 사유 페이지
  },

  ADMIN_MESSAGE: {
    tab: 'ADMIN',
    icon: 'admin_noti',
    title: '관리자 메시지',
    href: () => `#` // 링크 없음
  },

  INQUIRY_ANSWERED: {
    tab: 'ADMIN',
    icon: 'inquiry_noti',
    title: '문의 답변',
    href: (n) => `/my/inquiries/${n.targetId}` // 문의 검색
  },

  DELIVERY_START: {
    tab: 'DELIVERY',
    icon: 'delivery_start_noti',
    title: '배송 시작',
    href: (n) => `/mypage/${n.targetId}` // 마이페이지 도서 상세
  },

  DELIVERY_ARRIVE: {
    tab: 'DELIVERY',
    icon: 'delivery_arrive_noti',
    title: '배송 도착',
    href: (n) => `/mypage/${n.targetId}` // 마이페이지 도서 상세
  }
};

function getNotiMeta(n) {
  return NOTI_META[n.targetType] ?? {
    tab: 'ALL',
    icon: 'default',
    title: '알림',
    href: () => '#'
  };
}

// 사이트에 머무는 동안 새 알림을 불러오는 폴링
let pollingTimer = null;

async function poll() {
  try {
    const res = await fetch('/notifications/unread/count');

    // 선택: 세션 만료/로그아웃이면 중지
    if (res.status === 401 || res.status === 403) {
      stopNotificationPolling();
      return;
    }

    const data = await res.json();
    updateAlarmCnt(data.count);
  } catch (e) {
    console.error('알림 폴링 실패', e);
  }
}

function startNotificationPolling() {
  if (pollingTimer) return;
  poll();
  pollingTimer = setInterval(poll, 30000); // 30초
}

function stopNotificationPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
}

function updateAlarmCnt(count) {
  const cnt = document.querySelector('.alarm-cnt');
  if (count > 9) {
    cnt.textContent = '9+';
    cnt.style.display = 'block';
  } else if (count > 0) {
    cnt.textContent = count;
    cnt.style.display = 'block';
  } else {
    cnt.style.display = 'none';
  }
}


function getNotiHref(n) {
  const meta = getNotiMeta(n);
  return typeof meta.href === 'function' ? meta.href(n) : '#';
}

function getNotiIconUrl(n) {
  const meta = getNotiMeta(n);
  return `/images/noti/${meta.icon}.png`; // 네 아이콘 경로에 맞게 변경
}

function timeAgo(iso) {
  const diff = (Date.now() - new Date(iso)) / 1000;

  if (diff < 60) return '방금 전';
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
  return `${Math.floor(diff / 86400)}일 전`;
}

function loadTab(url) {
  // 알람 리스트
  const alarm_ul = document.querySelector(".alarm-list");
  alarm_ul.innerHTML = "";

  fetch(url)
  .then((response) => response.json())
  .then((datas) => {
    datas.forEach((data) => {
      const href = getNotiHref(data);
      const iconUrl = getNotiIconUrl(data);
      const danger = data.targetType==='ACCOUNT_RESTRICTED' ? 'danger' : '';
      const li = document.createElement("li");
      
      li.classList.add("alarm-item")
      let li_html = '';

      if(data.checkYn === 'N') li_html += `<span class="alarm-dot"></span>`;

      li_html += `<img src="${iconUrl}" class="alarm-icon">
                  <div class="alarm-text">
                      <p class="alarm-msg ${danger}">${data.notiContent}</p>
                      <span class="alarm-time">${timeAgo(data.createdAt)}</span>
                  </div>`;
      li.innerHTML = li_html;

      li.addEventListener("click", async () => {
        if (data.checkYn === 'N') fetch(`/notification/${data.notiId}/read`);
        window.location.href = href;
      });

      alarm_ul.appendChild(li);
    });
    
  });
}
