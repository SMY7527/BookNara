document.addEventListener("DOMContentLoaded", () => {

    /* ================= 공통 DOM ================= */
    const editBtn = document.getElementById("editBtn");
    const saveBtn = document.getElementById("saveBtn");
    const form = document.querySelector(".info-form");
    const editableInputs = document.querySelectorAll(".editable");
    const addressBtn = document.getElementById("addressBtn");

    const genreCards = document.querySelectorAll(".genre-card");

    let editMode = false;
    let selectedGenres = [];

    /* ================= 초기 장르 상태 수집 ================= */
    genreCards.forEach(card => {
        if (card.classList.contains("active")) {
            selectedGenres.push(Number(card.dataset.genreId));
        }
    });

    /* =================  submit 가드 추가 ================= */
    form?.addEventListener("submit", () => {
        editableInputs.forEach(input => input.disabled = false);
        if (addressBtn) addressBtn.disabled = false;
    });



    /* ================= 수정 버튼 ================= */
    editBtn?.addEventListener("click", () => {
        editMode = !editMode;

        // 회원정보 input
        editableInputs.forEach(input => {
            input.disabled = !editMode;
            input.style.background = editMode ? "#fff" : "#f3f3f3";
        });

        // 주소 버튼
        if (addressBtn) addressBtn.disabled = !editMode;

        // 저장 버튼
        if (saveBtn) saveBtn.style.display = editMode ? "inline-block" : "none";

        // 장르 수정 가능/불가
        genreCards.forEach(card => {
            card.classList.toggle("disabled", !editMode);
            card.dataset.editable = editMode ? "true" : "false";
        });

        editBtn.innerText = editMode ? "취소" : "수정";
    });

    /* ================= 장르 클릭 ================= */
    genreCards.forEach(card => {
        card.addEventListener("click", () => {
            if (card.dataset.editable !== "true") return;

            const id = Number(card.dataset.genreId);

            if (card.classList.contains("active")) {
                card.classList.remove("active");
                selectedGenres = selectedGenres.filter(v => v !== id);
            } else {
                if (selectedGenres.length >= 3) {
                    alert("장르는 최대 3개까지 선택할 수 있어요");
                    return;
                }
                card.classList.add("active");
                selectedGenres.push(id);
            }
        });
    });

    /* ================= submit 시 disabled 해제 (최중요) ================= */
    form?.addEventListener("submit", () => {
        editableInputs.forEach(input => input.disabled = false);
        if (addressBtn) addressBtn.disabled = false;
    });

    /* ================= 저장 버튼 ================= */
    saveBtn?.addEventListener("click", async (e) => {
        e.preventDefault();

        try {
            // 1️⃣ 장르 먼저 저장
            const res = await fetch("/mypage/myinfo/genres", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(selectedGenres)
            });
            if (!res.ok) throw new Error("genre save failed");

            // 2️⃣ 회원정보 저장 (submit 이벤트에서 disabled 해제됨)
            form.submit();

        } catch (err) {
            console.error(err);
            alert("저장 중 오류가 발생했습니다.");
        }
    });

});

/* ================= 다음 주소 API ================= */
window.execDaumPostcode = function () {
    if (!window.daum || !daum.Postcode) {
        alert("다음 주소 API가 로드되지 않았습니다.");
        return;
    }

    new daum.Postcode({
        oncomplete: function (data) {

            const zipcodeInput = document.querySelector("input[name='zipcode']");
            const addrInput = document.querySelector("input[name='addr']");
            const detailInput = document.querySelector("input[name='detailAddr']");

            if (zipcodeInput) {
                zipcodeInput.value = data.zonecode;
            }

            const baseAddr =
                (data.roadAddress && data.roadAddress !== "")
                    ? data.roadAddress
                    : data.jibunAddress;

            if (addrInput) {
                addrInput.value = baseAddr;
            }

            // 상세주소로 포커스 이동
            if (detailInput) {
                detailInput.focus();
            }
        }
    }).open();
};
