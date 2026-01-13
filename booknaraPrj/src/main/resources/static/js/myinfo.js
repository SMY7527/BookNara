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



/* ================= 비밀번호 재설정 ================= */
const pwModal = document.getElementById("pwModal");
const pwChangeBtn = document.getElementById("pwChangeBtn");
const pwCancelBtn = document.getElementById("pwCancelBtn");

if (pwModal && pwChangeBtn && pwCancelBtn) {

  pwChangeBtn.addEventListener("click", () => {
    resetPwModal();
    pwModal.style.display = "flex";
  });

  pwCancelBtn.addEventListener("click", () => {
    pwModal.style.display = "none";
  });


}

function resetPwModal() {
  const currentPw = document.getElementById("currentPw");
  const newPw = document.getElementById("newPw");
  const newPwConfirm = document.getElementById("newPwConfirm");
  const pwSubmitBtn = document.getElementById("pwSubmitBtn");
  const currentPwMsg = document.getElementById("currentPwMsg");
    const newPwMsg = document.getElementById("newPwMsg");
    const newPwConfirmMsg = document.getElementById("newPwConfirmMsg");

  if (currentPw) currentPw.value = "";
  if (newPw) {
    newPw.value = "";
    newPw.disabled = true;
  }
  if (newPwConfirm) {
    newPwConfirm.value = "";
    newPwConfirm.disabled = true;
  }
  if (pwSubmitBtn) pwSubmitBtn.disabled = true;

  showMsg(currentPwMsg, "");
    showMsg(newPwMsg, "");
    showMsg(newPwConfirmMsg, "");
}


// ==== 현재 비밀번호 검증 ====
const verifyPwBtn = document.getElementById("verifyPwBtn");
const currentPwInput = document.getElementById("currentPw");
const newPwInput = document.getElementById("newPw");
const newPwConfirmInput = document.getElementById("newPwConfirm");
const currentPwMsg = document.getElementById("currentPwMsg");

verifyPwBtn?.addEventListener("click", async () => {
  const currentPw = currentPwInput.value.trim();
  showMsg(currentPwMsg, "");

  if (!currentPw) {
    showMsg(currentPwMsg, "현재 비밀번호를 입력해 주세요.");
    currentPwInput.focus();
    return;
  }

  try {
    const res = await fetch("/mypage/password/verify", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ password: currentPw })
    });

    if (!res.ok) {
      const msg = await res.text();
      showMsg(currentPwMsg, msg || "비밀번호가 일치하지 않습니다.");
      return;
    }

    // ✅ 성공
    showMsg(currentPwMsg, "✔ 확인되었습니다.", true);
    newPwInput.disabled = false;
    newPwConfirmInput.disabled = false;
    newPwInput.focus();

  } catch (e) {
     showMsg(currentPwMsg, "네트워크 오류가 발생했습니다.");
  }

});

const pwSubmitBtn = document.getElementById("pwSubmitBtn");
const newPwMsg = document.getElementById("newPwMsg");
const newPwConfirmMsg = document.getElementById("newPwConfirmMsg");

// 비밀번호 규칙 (서비스랑 동일)
const pwRegex =
  /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,16}$/;

function validateNewPassword() {
  const currentPw = currentPwInput.value;
  const newPw = newPwInput.value;
  const confirmPw = newPwConfirmInput.value;

  showMsg(newPwMsg, "");
    showMsg(newPwConfirmMsg, "");

  // 정규식
  if (!pwRegex.test(newPw)) {
  showMsg(newPwMsg, "영문, 숫자, 특수문자 포함 8~16자로 입력해 주세요.");
    pwSubmitBtn.disabled = true;
    return;
  }

  // 새 비밀번호 확인
  if (newPw !== confirmPw) {
    showMsg(newPwConfirmMsg, "비밀번호가 일치하지 않습니다.");
    pwSubmitBtn.disabled = true;
    return;
  }

  // 기존 비밀번호와 동일 방지
  if (newPw === currentPw) {
    showMsg(newPwMsg, "기존 비밀번호와 동일합니다.");
    pwSubmitBtn.disabled = true;
    return;
  }

   if (confirmPw) {
      showMsg(newPwConfirmMsg, "✔ 비밀번호가 일치합니다.", true);
    }

  pwSubmitBtn.disabled = false;
}

newPwInput?.addEventListener("input", validateNewPassword);
newPwConfirmInput?.addEventListener("input", validateNewPassword);

// ==== 비밀번호 변경 ====
pwSubmitBtn?.addEventListener("click", async () => {
  pwSubmitBtn.disabled = true;

  try {
    const res = await fetch("/mypage/password/change", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ newPassword: newPwInput.value.trim() })
    });

    if (!res.ok) {
      const msg = await res.text();
      showMsg(newPwConfirmMsg, msg || "비밀번호 변경 실패");
      pwSubmitBtn.disabled = false;
      return;
    }

    alert("비밀번호가 변경되었습니다."); // ✅ 완료 알림만 유지
    pwModal.style.display = "none";

  } catch (e) {
    showMsg(newPwConfirmMsg, "네트워크 오류가 발생했습니다.");
    pwSubmitBtn.disabled = false;
  }


});

function showMsg(target, message, isSuccess = false) {
    if (!target) return;
    target.innerText = message;
    target.style.color = isSuccess ? "#28a745" : "#dc3545";
    target.style.display = message ? "block" : "none";
}