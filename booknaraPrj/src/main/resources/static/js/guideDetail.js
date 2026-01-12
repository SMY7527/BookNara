const GUIDES = [
  {
    id: 1,
    tag: "안내",
    title: "대여정보 안내",
    body: "대여 기간은 기본 14일입니다.\n\n- 연장: 1회 가능(연체 중 불가)\n- 연체: 연체일수만큼 대여 제한이 적용될 수 있습니다.",
  },
  {
    id: 2,
    tag: "배송",
    title: "배송비 안내",
    body: "기본 배송비 3,000원 / 30,000원 이상 무료 배송\n\n- 도서 무게/지역에 따라 예외가 있을 수 있습니다.",
  },
  {
    id: 3,
    tag: "정책",
    title: "반송정보 안내",
    body: "오배송/파손 시 교환 및 반품이 가능합니다.\n\n- 수령 후 7일 이내 접수\n- 사진 첨부 필수",
  },
  {
    id: 4,
    tag: "위치",
    title: "도서관 위치 안내",
    body: "운영시간: 09:00~18:00(주말/공휴일 휴무)\n\n- 주소: (예시) 서울시 ○○구 ○○로 00\n- 문의: 000-0000-0000",
  },
  {
    id: 5,
    tag: "수령",
    title: "수거함 위치 안내",
    body: "반납함/수거함은 도서관 정문 우측에 있습니다.\n\n- 분실 방지를 위해 반납 후 알림을 확인하세요.",
  },
];

/*
const params = new URLSearchParams(location.search);
const id = Number(params.get("id"));
*/

//const idModel  =  '[[ ${id} ]]';
console.log("^^^^");
console.log(idModel );
const id = Number(idModel );

const item = GUIDES.find((x) => x.id === id);


console.log( "item" , item);
const el = document.getElementById("detail");

if (!item) {
  el.innerHTML = `<div class="content"><div class="title">존재하지 않는 안내입니다.</div></div>`;
} else {
  el.innerHTML = `
    <div class="content">
      <div class="kicker">
        <span class="badge">${item.tag}</span>
        <span class="meta">이용안내</span>
      </div>
      <h3 class="title">${item.title}</h3>
      <div class="text">${item.body}</div>
    </div>
  `;
}
