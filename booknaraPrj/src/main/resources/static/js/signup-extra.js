
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

  function submitAddress() {
    const zipcode = document.querySelector('input[name="zipcode"]').value;
    const addr = document.querySelector('input[name="addr"]').value;
    const detailAddr = document.querySelector('input[name="detailAddr"]').value;


      if (!zipcode || !addr) {
        alert("주소 찾기를 먼저 해주세요");
        return;
      }

      if (!detailAddr) {
        alert("상세주소를 입력해주세요");
        document.querySelector('input[name="detailAddr"]').focus();
        return;
      }

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
      closeAddressPanel();
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


// 장르선택

const genreCards = document.querySelectorAll(".genre-card");
let selectedGenres = [];

genreCards.forEach(card => {
    card.addEventListener("click", () => {
        const value = Number(card.dataset.genreId);

        // 이미 선택 → 취소
        if (card.classList.contains("active")) {
            card.classList.remove("active");
            selectedGenres = selectedGenres.filter(v => v !== value);
        }
        // 새로 선택
        else {
            if (selectedGenres.length >= 3) {
                alert("장르는 최대 3개까지 선택할 수 있어요");
                return;
            }
            card.classList.add("active");
            selectedGenres.push(value);
        }

        updateDisabledState();
    });
});

function updateDisabledState() {
    if (selectedGenres.length >= 3) {
        genreCards.forEach(card => {
            if (!card.classList.contains("active")) {
                card.classList.add("disabled");
            }
        });
    } else {
        genreCards.forEach(card => card.classList.remove("disabled"));
    }
}

function submitGenres() {
    if (selectedGenres.length === 0) {
        alert("장르를 하나 이상 선택하세요 ");
        return;
    }

    fetch("/users/prefer-genres", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            genreIds: selectedGenres
        })
    })
    .then(res => {
        if (!res.ok) throw new Error();
        location.href = "/home";
    })
    .catch(() => {
        alert("장르 저장 중 오류가 났습니다");
    });
}
function skipGenres() {
     fetch("/users/extra-complete", {
            method: "POST"
        })
        .then(() => {
            location.href = "/home";
        });
}
