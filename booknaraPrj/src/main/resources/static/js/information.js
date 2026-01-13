const FAQS = [
  {
    id: 1,
    tag: "배송",
    top: true,
    q: "주문한 책과 다른 책이 배송되었어요.",
    a: "오배송이 확인되면 교환/반품이 가능합니다. 주문번호와 수령 사진을 함께 고객센터에 접수해 주세요.",
  },
  {
    id: 2,
    tag: "상품",
    top: true,
    q: "주문한 책은 언제 배송되나요?",
    a: "평균 출고는 1~2영업일이며, 택배사 사정에 따라 1~3일 추가될 수 있습니다.",
  },
  {
    id: 3,
    tag: "상품",
    top: true,
    q: "배송 받은 책이 파손되었어요.",
    a: "박스/상품 파손 사진을 찍어 접수해주시면 빠르게 재배송 또는 환불로 처리해 드립니다.",
  }
];

const NOTICES = [
  { id: 1, tag: "공지", title: "공식 연락처 안내", date: "2025-12-30" },
  { id: 2, tag: "", title: "2025/11/14 검색 불가 오류", date: "2025-11-14" },
  { id: 3, tag: "", title: "2025/08/20 일부 서비스 오류", date: "2025-08-20" }
];

const listEl = document.getElementById("noticeList");
const topEl = document.getElementById("topList");
//const listEl = document.getElementById("faqList");

let keyword = "";
let page = 1;
const pageSize = 5;

// 🔎 검색 필터
function filtered() {
  const k = keyword.trim();
  if (!k) return FAQS;
  return FAQS.filter((x) => (x.q + " " + x.a + " " + x.tag).includes(k));
}

// FAQ 아이템 생성(아코디언)
function makeItem(item) {
  const li = document.createElement("li");
  li.className = "faq-item";

  li.innerHTML = `
    <button class="faq-q" type="button">
      <div class="q-left">
        <span class="badge">${item.tag}</span>
        <span class="q-title">${item.q}</span>
      </div>
      <span class="chev">⌄</span>
    </button>
    <div class="faq-a">
      <div class="answer-box">${item.a}</div>
    </div>
  `;

  li.querySelector(".faq-q").addEventListener("click", () => {
    const parent = li.parentElement;
    const opened = parent.querySelector(".faq-item.open");
    if (opened && opened !== li) opened.classList.remove("open");
    li.classList.toggle("open");
  });
  console.log(li);

  return li;
}

function render() {
  const data = filtered();

  // ✅ TOP3는 검색과 무관하게 항상 고정하고 싶으면 FAQS 기준으로:
  // const topData = FAQS.filter(x => x.top).slice(0,3);

  // ✅ 검색 시 TOP도 같이 줄어들게(현재 방식):
  const topData = data.filter((x) => x.top).slice(0, 3);

  // 1) TOP 렌더
  topEl.innerHTML = "";
  if (topData.length === 0) {
    const li = document.createElement("li");
    li.className = "faq-item";
    li.innerHTML = `
      <button class="faq-q" type="button">
        <div class="q-left">
          <span class="badge">TOP</span>
          <span class="q-title">TOP 항목이 없습니다.</span>
        </div>
        <span class="chev">⌄</span>
      </button>
    `;
    topEl.appendChild(li);
  } else {
    topData.forEach((item) => topEl.appendChild(makeItem(item)));
  }


  // 공지사항 리스트
  listEl.innerHTML = "";
  if (NOTICES.length === 0) {
    const li = document.createElement("li");
    li.className = "notice-item";
    li.innerHTML = `<div class="notice-left">
      <span class="notice-title">표시할 공지사항이 없습니다.</span>
    </div>`;
    listEl.appendChild(li);
  } else {
    NOTICES.forEach((n) => {
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
}

//render();





function filteredData() {
  const k = keyword.trim();
  if (!k) return NOTICES;
  return NOTICES.filter((n) => (n.title + " " + n.tag).includes(k));
}

//function render() {
//  const data = filteredData();
//  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));
//  if (page > totalPages) page = totalPages;

//  const start = (page - 1) * pageSize;
//  const slice = data.slice(start, start + pageSize);

  // 리스트
//  listEl.innerHTML = "";
//  if (NOTICES.length === 0) {
//    const li = document.createElement("li");
//    li.className = "notice-item";
//    li.innerHTML = `<div class="notice-left">
//      <span class="notice-title">표시할 공지사항이 없습니다.</span>
//    </div>`;
//    listEl.appendChild(li);
//  } else {
//    NOTICES.forEach((n) => {
//      const li = document.createElement("li");
//      li.className = "notice-item";
//
//      const badge = n.tag
//        ? `<span class="badge">[${n.tag}]</span>`
//        : `<span class="badge" style="opacity:.0">[ ]</span>`;
//
//      li.innerHTML = `
//        <div class="notice-left">
//          ${badge}
//          <span class="notice-title">${n.title}</span>
//        </div>
//        <span class="notice-meta">${n.date} · 자세히 보기 →</span>
//      `;
//
//      li.addEventListener("click", () => {
//        location.href = `noticeDetail?id=${n.id}`;
//      });
//
//      listEl.appendChild(li);
//    });
//  }
//}

render();
