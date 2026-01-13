document.addEventListener("scroll", () => {
  const header = document.querySelector(".header");
  if (window.scrollY > 5) {
    header.classList.add("scrolled");
  } else {
    header.classList.remove("scrolled");
  }
});

document.addEventListener("DOMContentLoaded", () => {
  const alarmBtn = document.getElementById("alarmBtn");
  const alarmBox = document.getElementById("alarmBox");

  if (alarmBtn && alarmBox) {
    alarmBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      alarmBox.style.display = alarmBox.style.display === "block" ? "none" : "block";

      const tab = document.querySelector(".tab.active");
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
    href: () => '/my/booklist' // 내 서재
  },

  PAST_DUE: {
    tab: 'SYSTEM',
    icon: 'past_due_noti',
    title: '연체',
    href: () => '/my/booklist' // 내 서재
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
    href: (n) => `/my/booklist` // 마이페이지 도서 상세
  },

  DELIVERY_ARRIVE: {
    tab: 'DELIVERY',
    icon: 'delivery_arrive_noti',
    title: '배송 도착',
    href: (n) => `/my/booklist` // 마이페이지 도서 상세
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
