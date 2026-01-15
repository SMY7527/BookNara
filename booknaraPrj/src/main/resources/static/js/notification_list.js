// =========================
// State
// =========================
const state = {
  filter: "ALL",
  page: 1,
  pageSize: 10,
  totalRaw: 0,   // 전체 행 수
  grpSize: 10,   // 페이지 그룹 사이즈
  totalPages: 1
};

// =========================
// 유틸
// =========================
function escapeHtml(str) {
  return String(str ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// =========================
// 시간 포맷 (24시간 기준)
// createdAt: "2026-01-07T14:10:42"
// -> 한국 환경에서 대체로 로컬로 파싱되지만, 안정적으로 +09:00 붙여서 파싱
// =========================
function toKstDate(isoNoTz) {
  // "YYYY-MM-DDTHH:mm:ss" -> "YYYY-MM-DDTHH:mm:ss+09:00"
  if (typeof isoNoTz !== "string") return new Date(isoNoTz);
  if (isoNoTz.includes("Z") || isoNoTz.includes("+")) return new Date(isoNoTz);
  return new Date(`${isoNoTz}+09:00`);
}

function formatTime24h(createdAt) {
  const dt = toKstDate(createdAt);
  const diffSec = Math.floor((Date.now() - dt.getTime()) / 1000);

  if (diffSec < 60) return "방금 전";
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;

  const yyyy = dt.getFullYear();
  const mm = String(dt.getMonth() + 1).padStart(2, "0");
  const dd = String(dt.getDate()).padStart(2, "0");
  return `${yyyy}.${mm}.${dd}`;
}

// =========================
// targetType 매핑
// tab 값은 SYSTEM/COMMUNITY/EVENT/ADMIN/ALL
// =========================  delinquency_noti
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
    href: (n) => `/my/inquiry` // 문의 검색
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

function getNotiHref(n) {
  const meta = getNotiMeta(n);
  return typeof meta.href === 'function' ? meta.href(n) : '#';
}

function getNotiIconUrl(n) {
  const meta = getNotiMeta(n);
  return `/images/noti/${meta.icon}.png`;
}


document.addEventListener("DOMContentLoaded", () => {
  // =========================
  // API 엔드포인트
  // =========================
  // const LIST_API = "/notification?tab=all&page=0&size=10"";
  const LIST_API = (tab, p, s) => `/notification?tab=${encodeURIComponent(tab)}&page=${p}&size=${s}`;
  const READ_ONE_API = (id) => `/notification/${encodeURIComponent(id)}/read`;
  const READ_ALL_API = (filter) => `/notification/read-all/${encodeURIComponent(filter)}`;
  const COUNT_LIST = (filter) => `/notification/totalRaw?tab=${encodeURIComponent(filter)}`;

  // =========================
  // DOM
  // =========================
  const tabs = document.querySelectorAll(".noti-tabs .tab");
  const btnReadAll = document.getElementById("btnReadAll");

  const listEl = document.getElementById("notiList");
  const emptyEl = document.getElementById("notiEmpty");

  const btnPrev = document.getElementById("btnPrev");
  const btnNext = document.getElementById("btnNext");
  const pageNumbers = document.getElementById("pageNumbers");
  const pageInfo = document.getElementById("pageInfo");


  // =========================
  // 리스트 렌더
  // =========================
  function renderList() {
    if (!listEl) return;

    listEl.innerHTML = "";

    // fetch(url)
    let url = LIST_API(state.filter, state.page, state.pageSize);
    url += state.filter === 'UNREAD' ? '&checkYn=N' : '';

    fetch(url)
    .then((response) => response.json())
    .then((datas) => {
      if (!datas || datas.length === 0) {
        emptyEl.style.display = "block";
        return;
      }
      emptyEl.style.display = "none";

      datas.forEach(n => {
        const li = document.createElement("li");
        const unread = String(n.checkYn).toUpperCase() === "N";
        const iconUrl = getNotiIconUrl(n);
        const href = getNotiHref(n);
        const danger = n.targetType === "ACCOUNT_RESTRICTED" ? "danger" : "";

        li.className = `noti-item ${unread ? "" : "is-read"}`;
        li.dataset.id = n.notiId;

        li.innerHTML = `
          <div class="noti-icon-wrap">
            <img class="noti-icon" src="${iconUrl}" alt="">
          </div>
          <div class="noti-content">
            <div class="line1">
              ${unread ? `<span class="noti-badge-dot" title="미읽음"></span>` : ``}
              <div class="noti-text ${danger}">${escapeHtml(n.notiContent)}</div>
            </div>
            <div class="noti-meta">${formatTime24h(n.createdAt)}</div>
          </div>
          <div class="alarm-menu-dot">
            <img src="/images/noti/dotdotdot.png">
            <div class="alarm-menu">
              <button class="read-btn" ${unread ? '' : 'disabled'}>읽음 표시</button>
            </div>
          </div>
        `;

        li.addEventListener("click", async () => {
          try {
            if (unread) {
              const res = await fetch(READ_ONE_API(n.notiId), { method: "PATCH" }).catch(() => {});
              if(res.ok) {
                // 로컬 상태 반영
                n.checkYn = "Y";
                li.classList.add("is-read");
                const dot = li.querySelector(".noti-badge-dot");
                if (dot) dot.remove();
              }
            }

            if (href && href !== "#") window.location.href = href;
          } catch (e) {
            console.error(e);
          }
        });

        const alarmMenuDot = li.querySelector(".alarm-menu-dot");
        const alarmMenu = li.querySelector(".alarm-menu");
        const btn = li.querySelector(".read-btn");

        alarmMenuDot.addEventListener("click", (e) => {
          e.stopPropagation();
          
          const menuDots = document.querySelectorAll(".alarm-menu-dot");
          const menus = document.querySelectorAll(".alarm-menu");

          menuDots.forEach((dot) => {
            if(dot !== alarmMenuDot) dot.classList.remove("alarm-menu-active");
          });
          alarmMenuDot.classList.toggle("alarm-menu-active");

          menus.forEach((menu) => {
            if(menu !== alarmMenu) menu.style.display = "none";
          });
          alarmMenu.style.display = alarmMenu.style.display === "flex" ? "none" : "flex";
        });

        btn.addEventListener("click", async (e) => {
          e.stopPropagation();
          const res = await fetch(READ_ONE_API(n.notiId), { method: "PATCH" }).catch(() => {});
          if(res.ok) {
            // 로컬 상태 반영
            n.checkYn = "Y";
            li.classList.add("is-read");
            const dot = li.querySelector(".noti-badge-dot");
            if (dot) dot.remove();
          }
        });
        listEl.appendChild(li);
      });
    });
    
    renderPagination(state.filter);
  }

  // =========================
  // 페이지네이션 렌더
  // =========================
  async function getTotalRaw(tab) {
    let url = COUNT_LIST(tab);
    url += tab === 'UNREAD' ? '&checkYn=N' : '';
    
    const res = await fetch(url);
    state.totalRaw = Number(await res.text());
  }

  async function renderPagination(tab) {
    if (!btnPrev || !btnNext || !pageNumbers || !pageInfo) return;
    await getTotalRaw(tab);

    let currGrp;
    let totalPage;
    let grpStartPage;
    let grpEndPage;

    // 총 페이지수 구하기
    if (state.totalRaw % state.pageSize == 0)
        totalPage = state.totalRaw / state.pageSize;
    else
        totalPage = parseInt(state.totalRaw / state.pageSize) + 1;
    state.totalPages = totalPage;

    // 현재 그룹
    if (state.page % state.grpSize == 0)
        currGrp = state.page / state.grpSize;
    else
        currGrp = parseInt(state.page / state.grpSize) + 1;

    grpStartPage = (currGrp - 1) * state.grpSize + 1;  // 그룹의 시작번호   현재그룹 1 => 1  , 현재그룹 2 -> 6  
    grpEndPage = currGrp * state.grpSize;
    grpEndPage = grpEndPage >= totalPage ? totalPage : grpEndPage;
    
    pageInfo.textContent = state.totalRaw ? `총 ${state.totalRaw}개 · ${state.page} / ${totalPage} 페이지` : "";

    btnPrev.disabled = state.page <= 1;
    btnNext.disabled = state.page >= totalPage;

    pageNumbers.innerHTML = "";
    if (totalPage <= 1) return;

    for (let p = grpStartPage; p <= grpEndPage; p++) {
      const b = document.createElement("button");
      b.type = "button";
      b.textContent = String(p);
      if (p === state.page) b.classList.add("is-active");
      b.addEventListener("click", () => {
        state.page = p;
        renderList();
      });
      pageNumbers.appendChild(b);
    }
  }


  // =========================
  // Events
  // =========================
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("is-active", "active"));
      tab.classList.add("is-active", "active");

      state.filter = tab.dataset.filter || "all";
      state.page = 1;
      renderList();
    });
  });

  btnPrev?.addEventListener("click", () => {
    if (state.page <= 1) return;
    state.page -= 1;
    renderList();
  });

  btnNext?.addEventListener("click", () => {
    if (state.page >= state.totalPages) return;
    state.page += 1;
    renderList();
  });

  btnReadAll?.addEventListener("click", async () => {
    const ok = confirm("현재 탭의 알림을 모두 읽음 처리할까요?");
    if (!ok) return;

    try {
      await fetch(READ_ALL_API(state.filter), { method: "PATCH" });

      // 로컬도 즉시 반영
      // const before = state.filter;
      const notRead = document.querySelectorAll(".noti-item:not(.is-read)");
      notRead.forEach(li => {
        li.classList.add("is-read");
        const dot = li.querySelector(".noti-badge-dot");
        if (dot) dot.remove();
      });

      renderList();

    } catch (e) {
      console.error(e);
      alert("모두 읽음 처리에 실패했습니다.");
    }
  });

  // =========================
  // 초기 상태: URL 파라미터(선택)
  // /notification/list?tab=unread&page=2
  // =========================
  const params = new URLSearchParams(location.search);
  const tabFromUrl = params.get("tab");
  const pageFromUrl = params.get("page");

  if (tabFromUrl) state.filter = tabFromUrl;
  if (pageFromUrl && !Number.isNaN(Number(pageFromUrl))) state.page = Number(pageFromUrl);

  // 탭 표시 초기화
  if (tabs && tabs.length) {
    tabs.forEach(t => t.classList.remove("is-active", "active"));
    const initTab = Array.from(tabs).find(t => t.dataset.filter === state.filter) || tabs[0];
    initTab.classList.add("is-active", "active");
  }

  document.addEventListener("click", () => {
    const menuDots = document.querySelectorAll(".alarm-menu-dot");
    const menus = document.querySelectorAll(".alarm-menu");

    menuDots.forEach((dot) => { dot.classList.remove("alarm-menu-active"); });
    menus.forEach((menu) => { menu.style.display = "none"; });
  });

  // 최초 로딩
  renderList();
});
