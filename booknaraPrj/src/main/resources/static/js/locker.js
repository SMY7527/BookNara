(() => {
  const stations = [
    "홍대입구역",
    "합정역",
    "신촌역",
    "이대역",
    "당산역",
    "디지털미디어시티역",
    "상수역",
    "망원역",
    "가좌역",
    "공덕역",
  ];

  const $ = (sel) => document.querySelector(sel);
  const listEl = $("#stationList");
  const inputEl = $("#searchInput");

  const toast = (msg) => {
    if (typeof window.showToast === "function") window.showToast(msg);
    else alert(msg);
  };

  function mapQuery(stationName) {
    // “역명 수거함”으로 검색 (필요하면 "무인택배함" 등으로 바꿔도 됨)
    return encodeURIComponent(`${stationName} `);
  }

  function openNaver(stationName) {
    window.open(
      `https://map.naver.com/v5/search/${mapQuery(stationName)}`,
      "_blank"
    );
  }

  function openKakao(stationName) {
    window.open(
      `https://map.kakao.com/link/search/${mapQuery(stationName)}`,
      "_blank"
    );
  }

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
      toast("복사했어!");
    } catch (e) {
      toast("복사 실패! 드래그해서 복사해줘.");
    }
  }

  function render(items) {
    if (!items.length) {
      listEl.innerHTML = `
        <li class="station-item">
          <div class="station-left">
            <span class="badge">안내</span>
            <span class="station-title">검색 결과가 없어요</span>
          </div>
          <span class="station-meta">다른 키워드로 검색해봐!</span>
        </li>
      `;
      return;
    }

    listEl.innerHTML = items
      .map(
        (name) => `
      <li class="station-item">
        <div class="station-left">
          <span class="badge">역</span>
          <span class="station-title">${name}</span>
        </div>
        <div class="station-actions">
          <button class="small-btn" type="button" data-copy="${name}">복사</button>
          <button class="small-btn" type="button" data-naver="${name}">네이버지도</button>
          <button class="small-btn" type="button" data-kakao="${name}">카카오맵</button>
        </div>
      </li>
    `
      )
      .join("");

    // 이벤트 바인딩
    listEl.querySelectorAll("[data-copy]").forEach((btn) => {
      btn.addEventListener("click", () => copyText(btn.dataset.copy));
    });
    listEl.querySelectorAll("[data-naver]").forEach((btn) => {
      btn.addEventListener("click", () => openNaver(btn.dataset.naver));
    });
    listEl.querySelectorAll("[data-kakao]").forEach((btn) => {
      btn.addEventListener("click", () => openKakao(btn.dataset.kakao));
    });
  }

  function applySearch() {
    const q = (inputEl.value || "").trim().toLowerCase();
    const filtered = !q
      ? stations
      : stations.filter((s) => s.toLowerCase().includes(q));
    render(filtered);
  }

  $("#searchBtn")?.addEventListener("click", applySearch);
  $("#resetBtn")?.addEventListener("click", () => {
    inputEl.value = "";
    applySearch();
  });

  inputEl?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") applySearch();
  });

  $("#copyAllBtn")?.addEventListener("click", () => {
    copyText(stations.join(", "));
  });

  // 초기 렌더
  render(stations);
})();
