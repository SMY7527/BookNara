// schedule.js
const DATA = [
  {
    region: "서울/경기",
    ship: "월~금 출고(15:00 마감)",
    pickup: "평일 방문 회수(요청 후 1~2일)",
    box: "수거함: 화/금 수거",
    note: "서울/경기는 기본적으로 가장 빠른 편이에요.",
  },
  {
    region: "인천",
    ship: "월~금 출고(15:00 마감)",
    pickup: "평일 방문 회수(요청 후 1~2일)",
    box: "수거함: 수/토 수거",
    note: "인천은 지역에 따라 회수일이 하루 늘 수 있어요.",
  },
  {
    region: "충청/강원",
    ship: "월/수/금 출고(15:00 마감)",
    pickup: "요청 후 2~3일 내 회수",
    box: "수거함: 목 1회 수거",
    note: "권역 특성상 배송/회수 모두 하루 정도 더 걸릴 수 있어요.",
  },
  {
    region: "전라/경상",
    ship: "화/목 출고(15:00 마감)",
    pickup: "요청 후 2~3일 내 회수",
    box: "수거함: 금 1회 수거",
    note: "택배사 사정/기상에 따라 지연될 수 있어요.",
  },
  {
    region: "제주/도서산간",
    ship: "주 1~2회 출고(별도 안내)",
    pickup: "요청 후 3~5일 내 회수",
    box: "수거함 운영 지역 제한",
    note: "추가 배송비/지연 가능성이 있어요. 공지/FAQ 확인 추천!",
  },
];

const regionSel = document.getElementById("regionSel");
const seg = document.getElementById("seg");
const noteEl = document.getElementById("note");
const weekList = document.getElementById("weekList");
const tblBody = document.getElementById("tblBody");

let mode = "ship"; // ship | pickup
let region = DATA[0].region;

function fillSelect() {
  regionSel.innerHTML = "";
  DATA.forEach((x) => {
    const opt = document.createElement("option");
    opt.value = x.region;
    opt.textContent = x.region;
    regionSel.appendChild(opt);
  });
  regionSel.value = region;
}

function dayName(d) {
  return ["일", "월", "화", "수", "목", "금", "토"][d];
}

function getWeekDates() {
  const now = new Date();
  const list = [];
  for (let i = 0; i < 7; i++) {
    const dt = new Date(now);
    dt.setDate(now.getDate() + i);
    list.push(dt);
  }
  return list;
}

function makeBadgeText() {
  return mode === "ship" ? "발송" : "회수";
}

function renderWeek() {
  const picked = DATA.find((x) => x.region === region);

  weekList.innerHTML = "";
  getWeekDates().forEach((dt) => {
    const y = dt.getFullYear();
    const m = String(dt.getMonth() + 1).padStart(2, "0");
    const dd = String(dt.getDate()).padStart(2, "0");
    const dow = dayName(dt.getDay());

    const li = document.createElement("div");
    li.className = "week-item";

    // 아주 단순 규칙(샘플): 주말은 “변동”
    const isWeekend = dt.getDay() === 0 || dt.getDay() === 6;
    const meta = isWeekend
      ? "주말/공휴일은 변동 가능"
      : mode === "ship"
      ? picked.ship
      : picked.pickup;

    li.innerHTML = `
      <div class="week-left">
        <div class="week-day">${m}.${dd} (${dow})</div>
        <div class="week-meta">${meta}</div>
      </div>
      <span class="week-badge">${makeBadgeText()}</span>
    `;
    weekList.appendChild(li);
  });

  // 노트 박스
  const noteText =
    mode === "ship"
      ? `선택 지역: ${picked.region}\n배송(발송): ${picked.ship}\n수거함 수거: ${picked.box}\n${picked.note}`
      : `선택 지역: ${picked.region}\n반납(회수): ${picked.pickup}\n수거함 수거: ${picked.box}\n${picked.note}`;

  noteEl.textContent = noteText;
}

function renderTable() {
  tblBody.innerHTML = "";
  DATA.forEach((x) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${x.region}</td>
      <td>${x.ship}</td>
      <td>${x.pickup}</td>
      <td>${x.box}</td>
    `;
    tblBody.appendChild(tr);
  });
}

seg.addEventListener("click", (e) => {
  const btn = e.target.closest(".seg-btn");
  if (!btn) return;

  seg.querySelectorAll(".seg-btn").forEach((b) => b.classList.remove("active"));
  btn.classList.add("active");
  mode = btn.dataset.mode;
  renderWeek();
});

regionSel.addEventListener("change", () => {
  region = regionSel.value;
  renderWeek();
});

fillSelect();
renderTable();
renderWeek();
