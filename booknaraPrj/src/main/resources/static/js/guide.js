const GUIDES = [
  {
    id: 1,
    tag: "안내",
    title: "대여정보 안내",
    desc: "대여기간/연장/연체 기준",
  },
  { id: 2, tag: "배송", title: "배송비 안내", desc: "배송비/무료배송 조건" },
  { id: 3, tag: "정책", title: "반송정보 안내", desc: "반품/교환/오배송" },
  { id: 4, tag: "위치", title: "도서관 위치 안내", desc: "오시는 길/운영시간" },
  { id: 5, tag: "수령", title: "수거함 위치 안내", desc: "반납함/수거함 위치" }
//  { id: 6, tag: "회원", title: "회원등급 안내", desc: "등급/혜택/포인트" },
//  { id: 7, tag: "결제", title: "결제수단 안내", desc: "카드/계좌/간편결제" },
//  { id: 8, tag: "보안", title: "개인정보 처리 안내", desc: "약관/개인정보" },
];

const listEl = document.getElementById("guideList");
const pagingEl = document.getElementById("paging");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");

let keyword = "";
let page = 1;
const pageSize = 5;

function filtered() {
  const k = keyword.trim();
  if (!k) return GUIDES;
  return GUIDES.filter((g) =>
    (g.title + " " + g.desc + " " + g.tag).includes(k)
  );
}

function render() {
  const data = filtered();
  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));
  if (page > totalPages) page = totalPages;

  const start = (page - 1) * pageSize;
  const slice = data.slice(start, start + pageSize);

  listEl.innerHTML = "";
  if (slice.length === 0) {
    const li = document.createElement("li");
    li.className = "guide-item";
    li.innerHTML = `
      <div class="guide-left">
        <span class="badge">안내</span>
        <span class="guide-title">표시할 안내가 없습니다.</span>
      </div>
      <span class="guide-meta"></span>
    `;
    listEl.appendChild(li);
  } else {
    slice.forEach((g) => {
      const li = document.createElement("li");
      li.className = "guide-item";
      li.innerHTML = `
        <div class="guide-left">
          <span class="badge">${g.tag}</span>
          <span class="guide-title">${g.title}</span>
        </div>
        <span class="guide-meta">${g.desc} · 자세히 보기 →</span>
      `;


      li.addEventListener("click", () => {
      //  location.href = `guideDetail.html?id=${g.id}`;
          location.href =  `/guideDetail?id=${g.id}`;
      });

      listEl.appendChild(li);
    });
  }

  pagingEl.innerHTML = "";
  for (let i = 1; i <= totalPages; i++) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "page-btn" + (i === page ? " active" : "");
    btn.textContent = i;
    btn.addEventListener("click", () => {
      page = i;
      render();
    });
    pagingEl.appendChild(btn);
  }
}

function doSearch() {
  keyword = searchInput.value;
  page = 1;
  render();
}

searchBtn.addEventListener("click", doSearch);
searchInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") doSearch();
});

render();
