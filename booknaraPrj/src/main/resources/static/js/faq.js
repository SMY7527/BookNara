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
  },
  {
    id: 4,
    tag: "배송",
    q: "배송 날짜와 시간을 지정할 수 있나요?",
    a: "일반 택배는 시간 지정이 어렵습니다. 다만, 배송메모에 요청사항을 남기면 기사님이 참고할 수 있습니다.",
  },
  {
    id: 5,
    tag: "배송비",
    q: "배송비는 얼마인가요?",
    a: "기본 배송비는 3,000원이며, 30,000원 이상 구매 시 무료 배송입니다.",
  },
  {
    id: 6,
    tag: "회원",
    q: "비밀번호를 잊어버렸어요.",
    a: "로그인 화면의 ‘비밀번호 찾기’를 통해 이메일 인증 후 재설정할 수 있습니다.",
  },
  {
    id: 7,
    tag: "결제",
    q: "결제 수단은 어떤 게 있나요?",
    a: "신용/체크카드, 계좌이체, 간편결제(카카오/네이버 등)를 지원합니다.",
  },
  {
    id: 8,
    tag: "대여/반납",
    q: "대여 기간 연장은 가능한가요?",
    a: "대여 기간은 1회에 한해 연장 가능하며, 연체 중인 도서는 연장할 수 없습니다.",
  },
  {
    id: 9,
    tag: "정책",
    q: "반품/교환 기준이 궁금해요.",
    a: "수령 후 7일 이내 신청 가능하며, 훼손/파손 정도에 따라 처리 기준이 달라질 수 있습니다.",
  },
];

const topEl = document.getElementById("topList");
const listEl = document.getElementById("faqList");
const pagingEl = document.getElementById("paging");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");

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

  // 2) 일반 목록(Top 제외) + 페이징
  const normal = data.filter((x) => !x.top);
  const totalPages = Math.max(1, Math.ceil(normal.length / pageSize));
  if (page > totalPages) page = totalPages;

  const start = (page - 1) * pageSize;
  const slice = normal.slice(start, start + pageSize);

  listEl.innerHTML = "";
  if (slice.length === 0) {
    const li = document.createElement("li");
    li.className = "faq-item";
    li.innerHTML = `
      <button class="faq-q" type="button">
        <div class="q-left">
          <span class="badge">안내</span>
          <span class="q-title">표시할 질문이 없습니다.</span>
        </div>
        <span class="chev">⌄</span>
      </button>
    `;
    listEl.appendChild(li);
  } else {
    slice.forEach((item) => listEl.appendChild(makeItem(item)));
  }

  // 3) 페이지네이션 렌더
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

// URL에서 query 파라미터 읽기
const params = new URLSearchParams(window.location.search);
const q = params.get("q");

if (q) {
  keyword = q;                 // 기존 검색 로직에 연결
  searchInput.value = q;       // 검색창에도 자동 입력
  page = 1;
  render();                    // 바로 검색 결과 렌더
}