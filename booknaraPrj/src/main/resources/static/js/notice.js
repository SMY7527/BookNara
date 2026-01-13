const NOTICES = [
  { id: 1, tag: "공지", title: "공식 연락처 안내", date: "2025-12-30" },
  { id: 2, tag: "", title: "2025/11/14 검색 불가 오류", date: "2025-11-14" },
  { id: 3, tag: "", title: "2025/08/20 일부 서비스 오류", date: "2025-08-20" },
  { id: 4, tag: "", title: "서비스 이용약관 개정 공지", date: "2025-08-01" },
  { id: 5, tag: "공지", title: "이용 약관 개정 예정 안내", date: "2025-07-20" }
//  { id: 6, tag: "", title: "시스템 점검 안내", date: "2025-07-01" },
//  { id: 7, tag: "", title: "커뮤니티 운영정책 변경 안내", date: "2025-06-11" },
//  { id: 8, tag: "", title: "포인트 정책 안내", date: "2025-05-02" },
//  { id: 9, tag: "", title: "배송 관련 안내", date: "2025-04-19" },
];

const listEl = document.getElementById("noticeList");
const pagingEl = document.getElementById("paging");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");

let keyword = "";
let page = 1;
const pageSize = 5;

function filteredData() {
  const k = keyword.trim();
  if (!k) return NOTICES;
  return NOTICES.filter((n) => (n.title + " " + n.tag).includes(k));
}

function render() {
  const data = filteredData();
  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));
  if (page > totalPages) page = totalPages;

  const start = (page - 1) * pageSize;
  const slice = data.slice(start, start + pageSize);

  // 리스트
  listEl.innerHTML = "";
  if (slice.length === 0) {
    const li = document.createElement("li");
    li.className = "notice-item";
    li.innerHTML = `<div class="notice-left">
      <span class="notice-title">표시할 공지사항이 없습니다.</span>
    </div>`;
    listEl.appendChild(li);
  } else {
    slice.forEach((n) => {
      const li = document.createElement("li");
      li.className = "notice-item";

      const badge = n.tag
        ? `<span class="badge">[${n.tag}]</span>`
        : `<span class="badge" style="opacity:.0">[ ]</span>`;

      li.innerHTML = `
        <div class="notice-left">
          ${badge}
          <span class="notice-title">${n.title}</span>
        </div>
        <span class="notice-meta">${n.date} · 자세히 보기 →</span>
      `;

      li.addEventListener("click", () => {
        location.href = `noticeDetail?id=${n.id}`;
      });

      listEl.appendChild(li);
    });
  }

  // 페이지네이션
  pagingEl.innerHTML = "";
  for (let i = 1; i <= totalPages; i++) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "page-btn" + (i === page ? " active" : "");
    btn.textContent = i;
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
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
