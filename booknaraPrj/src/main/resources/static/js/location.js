(() => {
  const addr = "서교동 354-8"; // 너가 준 주소
  const addrEl = document.getElementById("addrText");
  const copyBtn = document.getElementById("copyAddr");

  // 외부 지도 “검색 링크”는 API가 아니라 그냥 검색 페이지 열기(선택사항)
  const q = encodeURIComponent(addr);
  const naver = `https://map.naver.com/v5/search/${q}`;
  const kakao = `https://map.kakao.com/link/search/${q}`;

  const naverLink = document.getElementById("naverLink");
  const kakaoLink = document.getElementById("kakaoLink");
  if (naverLink) naverLink.href = naver;
  if (kakaoLink) kakaoLink.href = kakao;

  if (addrEl) addrEl.textContent = addr;

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
      if (window.toast) toast("주소가 복사되었습니다!", "success");
      else alert("주소가 복사되었습니다!");
    } catch (e) {
      // 구형 브라우저 대비
      const ta = document.createElement("textarea");
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      ta.remove();
      if (window.toast) toast("주소가 복사되었습니다!", "success");
      else alert("주소가 복사되었습니다!");
    }
  }

  copyBtn?.addEventListener("click", () => copyText(addr));
})();
