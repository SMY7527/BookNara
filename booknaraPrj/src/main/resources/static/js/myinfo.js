// ================= 내정보 수정/저장 =================
const editBtn = document.getElementById("editBtn");
const saveBtn = document.getElementById("saveBtn");
const editableInputs = document.querySelectorAll(".editable");
const addressBtn = document.querySelector(".address-row button");

let editMode = false;

if (editBtn) {
    editBtn.addEventListener("click", () => {
        editMode = !editMode;

        editableInputs.forEach(input => {
            input.disabled = !editMode;
            input.style.background = editMode ? "#fff" : "#f3f3f3";
        });

        if (addressBtn) addressBtn.disabled = !editMode;
        if (saveBtn) saveBtn.style.display = editMode ? "inline-block" : "none";

        editBtn.innerText = editMode ? "취소" : "수정";
    });
}

// ================= (선택) 다음 주소 API =================
function execDaumPostcode() {
    if (!window.daum || !daum.Postcode) {
        alert("다음 주소 API 스크립트가 로드되지 않았어요. myinfo.html에서 스크립트 주석을 해제해 주세요.");
        return;
    }

    new daum.Postcode({
        oncomplete: function (data) {
            const addressInput = document.querySelector("input[name='address']");
            if (addressInput) addressInput.value = data.address;
        }
    }).open();
} // ✅ 여기 닫는 중괄호가 빠져서 오류였음

// ================= 회원탈퇴 모달 =================
const withdrawBtn = document.getElementById("withdrawBtn");
const withdrawModal = document.getElementById("withdrawModal");
const withdrawCloseBtn = document.getElementById("withdrawCloseBtn");
const withdrawCancelBtn = document.getElementById("withdrawCancelBtn");
const withdrawConfirmBtn = document.getElementById("withdrawConfirmBtn");

const captchaImg = document.getElementById("captchaImg");
const captchaRefreshBtn = document.getElementById("captchaRefreshBtn");
const captchaInput = document.getElementById("captchaInput");
const withdrawPassword = document.getElementById("withdrawPassword");

function openWithdrawModal() {
    if (!withdrawModal) return;
    withdrawModal.style.display = "flex";
    refreshCaptcha();
    if (captchaInput) captchaInput.value = "";
    if (withdrawPassword) withdrawPassword.value = "";
}

function closeWithdrawModal() {
    if (!withdrawModal) return;
    withdrawModal.style.display = "none";
}

function refreshCaptcha() {
    if (!captchaImg) return;
    captchaImg.src = "/mypage/withdraw/captcha?ts=" + Date.now();
}

if (withdrawBtn) withdrawBtn.addEventListener("click", openWithdrawModal);
if (withdrawCloseBtn) withdrawCloseBtn.addEventListener("click", closeWithdrawModal);
if (withdrawCancelBtn) withdrawCancelBtn.addEventListener("click", closeWithdrawModal);
if (captchaRefreshBtn) captchaRefreshBtn.addEventListener("click", refreshCaptcha);

// 오버레이 클릭하면 닫기(모달 내부 클릭은 제외)
if (withdrawModal) {
    withdrawModal.addEventListener("click", (e) => {
        if (e.target === withdrawModal) closeWithdrawModal();
    });
}

if (withdrawConfirmBtn) {
    withdrawConfirmBtn.addEventListener("click", async () => {
        const captcha = (captchaInput?.value || "").trim();
        const password = (withdrawPassword?.value || "").trim();

        if (!captcha) {
            alert("보안문자를 입력해 주세요.");
            captchaInput?.focus();
            return;
        }
        if (!password) {
            alert("비밀번호를 입력해 주세요.");
            withdrawPassword?.focus();
            return;
        }

        try {
            withdrawConfirmBtn.disabled = true;

            const formData = new URLSearchParams();
            formData.append("captcha", captcha);
            formData.append("password", password);

            const res = await fetch("/mypage/withdraw", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                body: formData.toString()
            });

            if (res.ok) {
                alert("회원 탈퇴 처리되었습니다.");
                window.location.href = "/";
                return;
            }

            const msg = await res.text();
            alert(msg || "탈퇴 처리에 실패했습니다.");
            refreshCaptcha();
        } catch (e) {
            alert("네트워크 오류가 발생했습니다.");
            refreshCaptcha();
        } finally {
            withdrawConfirmBtn.disabled = false;
        }
    });
}
