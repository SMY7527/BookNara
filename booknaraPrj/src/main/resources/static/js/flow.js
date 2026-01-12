// flow.js
document.querySelectorAll(".step-card").forEach((card) => {
  const btn = card.querySelector(".step-more");
  btn.addEventListener("click", () => {
    // 한 번에 하나만 열기
    const opened = document.querySelector(".step-card.open");
    if (opened && opened !== card) opened.classList.remove("open");
    card.classList.toggle("open");
  });
});

const QUICK = [
  { label: "배송이 늦어요", q: "배송" },
  { label: "반납은 어떻게 해요?", q: "반납" },
  { label: "수거함 사용법", q: "수거함" },
  { label: "연체/기간", q: "연체" },
  { label: "파손/오배송", q: "파손" },
  { label: "결제/환불", q: "환불" },
];

const chips = document.getElementById("chips");
chips.innerHTML = "";

QUICK.forEach((x) => {
  const b = document.createElement("button");
  b.type = "button";
  b.className = "chip";
  b.textContent = x.label;

  b.addEventListener("click", () => {
    // FAQ로 이동 (q는 선택: FAQ에서 query 파싱하면 자동검색 가능)
    location.href = `/root/FAQ/faq.html?q=${encodeURIComponent(x.q)}`;
  });

  chips.appendChild(b);
});