const EVENTS = [
  {
    id: 1,
    status: "ongoing",
    title: "청소년 배송비 할인 이벤트",
    desc: "청소년 인증 시 배송비 할인",
    period: "2026.01.01 ~ 01.31",
    img: "https://picsum.photos/seed/e1/600/400",
  },
  {
    id: 2,
    status: "closing",
    title: "수험생 배송비 무료",
    desc: "이번 주말까지 무료 배송",
    period: "2026.01.02 ~ 01.05",
    img: "https://picsum.photos/seed/e2/600/400",
  },
  {
    id: 3,
    status: "closing",
    title: "말랑이 레벨업 이벤트",
    desc: "구매 금액별 포인트 지급",
    period: "2026.01.01 ~ 01.07",
    img: "https://picsum.photos/seed/e3/600/400",
  },
  {
    id: 4,
    status: "ongoing",
    title: "다독자 이벤트",
    desc: "리뷰 작성 시 포인트",
    period: "2026.01.01 ~ 02.15",
    img: "https://picsum.photos/seed/e4/600/400",
  },
  {
    id: 5,
    status: "closed",
    title: "독서 후기 릴레이",
    desc: "이벤트 종료",
    period: "2025.12.01 ~ 12.31",
    img: "https://picsum.photos/seed/e5/600/400",
  },
];

const statusText = {
  ongoing: "진행중",
  closing: "마감임박",
  closed: "마감",
};

const grid = document.getElementById("eventGrid");
const tabs = document.querySelectorAll(".tab");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const moreBtn = document.getElementById("moreBtn");

let currentStatus = "ongoing";
let keyword = "";
let visible = 6;

function getFiltered() {
  return EVENTS.filter((e) => {
    const matchStatus =
      currentStatus === "ongoing"
        ? e.status === "ongoing" || e.status === "closing"
        : e.status === currentStatus;

    const matchKeyword = (e.title + e.desc).includes(keyword);

    return matchStatus && matchKeyword;
  });
}

function render() {
  const data = getFiltered();
  const slice = data.slice(0, visible);
  grid.innerHTML = "";

  slice.forEach((e) => {
    const card = document.createElement("div");
    card.className = "event-card";

    card.innerHTML = `
      <div class="thumb">
        <img src="${e.img}">
        <span class="badge ${e.status === "closing" ? "closing" : ""}">
          ${statusText[e.status]}
        </span>
      </div>
      <div class="body">
        <div class="title">${e.title}</div>
        <div class="desc">${e.desc}</div>
        <div class="meta">
          <span>${e.period}</span>
          <span>자세히 →</span>
        </div>
      </div>
    `;

    card.onclick = () => (location.href = `eventDetail.html?id=${e.id}`);

    grid.appendChild(card);
  });

  moreBtn.disabled = data.length <= visible;
}

tabs.forEach((tab) => {
  tab.onclick = () => {
    tabs.forEach((t) => t.classList.remove("active"));
    tab.classList.add("active");
    currentStatus = tab.dataset.status;
    visible = 6;
    render();
  };
});

searchBtn.onclick = () => {
  keyword = searchInput.value;
  visible = 6;
  render();
};

moreBtn.onclick = () => {
  visible += 6;
  render();
};

render();
