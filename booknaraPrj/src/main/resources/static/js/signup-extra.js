// ❗ 회원가입 페이지가 아니면 즉시 중단
if (!document.getElementById("addressPanel")) {
    // 마이페이지에서는 여기서 끝
    console.log("signup-extra.js: 회원가입 페이지 아님, 실행 중단");
} else {

    const panel = document.getElementById("addressPanel");
    const overlay = document.getElementById("overlay");
    const genreSection = document.getElementById("genreSection");

    // 처음 들어오면 주소 패널 자동 오픈
    alert("회원가입이 정상적으로 완료되었습니다.");

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
        if (genreSection) {
            genreSection.hidden = false;
        }
    }

    function submitAddress() {
        const zipcode = document.querySelector('input[name="zipcode"]')?.value;
        const addr = document.querySelector('input[name="addr"]')?.value;
        const detailAddr = document.querySelector('input[name="detailAddr"]')?.value;

        if (!zipcode || !addr) {
            alert("주소 찾기를 먼저 해주세요");
            return;
        }

        if (!detailAddr) {
            alert("상세주소를 입력해주세요");
            document.querySelector('input[name="detailAddr"]')?.focus();
            return;
        }

        fetch("/users/extra-address", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                zipcode: zipcode,
                addr: addr,
                detailAddr: detailAddr
            })
        }).then(() => {
            closeAddressPanel();
        });
    }

    // 다음 주소 API
    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function (data) {

                document.querySelector('input[name="zipcode"]').value = data.zonecode;

                const addr =
                    data.roadAddress && data.roadAddress !== ""
                        ? data.roadAddress
                        : data.jibunAddress;

                document.querySelector('input[name="addr"]').value = addr;
                document.querySelector('input[name="detailAddr"]').focus();
            }
        }).open();
    }

    // ================= 장르 선택 (회원가입 전용) =================
    const genreCards = document.querySelectorAll(".genre-card");
    let selectedGenres = [];

    genreCards.forEach(card => {
        card.addEventListener("click", () => {
            const value = Number(card.dataset.genreId);

            if (card.classList.contains("active")) {
                card.classList.remove("active");
                selectedGenres = selectedGenres.filter(v => v !== value);
            } else {
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
            alert("장르를 하나 이상 선택하세요");
            return;
        }

        fetch("/users/prefer-genres", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ genreIds: selectedGenres })
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
        fetch("/users/extra-complete", { method: "POST" })
            .then(() => location.href = "/home");
    }

}
