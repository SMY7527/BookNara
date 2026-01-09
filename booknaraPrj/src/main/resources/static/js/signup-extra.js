
  const panel = document.getElementById("addressPanel");
  const overlay = document.getElementById("overlay");
  const genreSection = document.getElementById("genreSection");

  // 처음 들어오면 주소 패널 자동 오픈
  alert("회원가입이 정상적으로 완료되었습니다.")
  window.addEventListener("DOMContentLoaded", () => {
    openAddressPanel();
  });

  function openAddressPanel() {
    overlay.classList.add("is-open");
    panel.classList.add("is-open");
  }

  function closeAddressPanel() {
    panel.classList.remove("is-open");
    panel.classList.add("is-exit");

    setTimeout(() => {
      panel.classList.remove("is-exit");
      overlay.classList.remove("is-open");

      // ⭐ 주소 입력/건너뛰기 상관없이 → 장르 선택
      showGenreSection();
    }, 300);
  }

  function showGenreSection() {
    genreSection.hidden = false;
  }

  function submitGenres() {
    const zipcode = document.querySelector('input[name="zipcode"]').value;
    const addr = document.querySelector('input[name="addr"]').value;
    const detailAddr = document.querySelector('input[name="detailAddr"]').value;

    fetch("/users/extra-address", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        zipcode: zipcode,
        addr: addr,
        detailAddr: detailAddr
      })
    }).then(() => {
      location.href = "/home";
    });
  }

function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function (data) {

      // 우편번호
      document.querySelector('input[name="zipcode"]').value = data.zonecode;

      // 도로명 주소 우선
      const addr = data.roadAddress && data.roadAddress !== ""
        ? data.roadAddress
        : data.jibunAddress;

      document.querySelector('input[name="addr"]').value = addr;

      // 상세주소로 포커스 이동
      document.querySelector('input[name="detailAddr"]').focus();
    }
  }).open();
}
