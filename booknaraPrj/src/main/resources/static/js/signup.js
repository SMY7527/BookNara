const userIdInput = document.getElementById("userIdInput");
const userIdMsg = document.getElementById("userIdMsg");

const pwInput = document.getElementById("password");
const pwMsg = document.getElementById("pwMsg");

const pwConfirmInput = document.getElementById("passwordConfirm");
const pwConfirmMsg = document.getElementById("pwConfirmMsg");

const profileNameInput = document.getElementById("profileNameInput");
const profileNameMsg = document.getElementById("profileNameMsg");

const emailInput = document.querySelector("input[name='email']");

// ✅ 초기값 true (다른 기능 안 막히게)
let isEmailAvailable = true;

const form = document.querySelector("form");

/* =========================
   ⭐ 상태값
========================= */
let isUserIdChecked = false;
let isProfileNameAvailable = false;

/* =========================
   아이디 중복 체크
========================= */
function checkUserId() {
    const userId = userIdInput.value.trim();
    const regex = /^[a-zA-Z0-9]{4,12}$/;

    if (!regex.test(userId)) {
        userIdMsg.textContent = "영문/숫자 4~12자";
        userIdMsg.className = "msg error";
        isUserIdChecked = false;
        return;
    }

    fetch(`/api/users/check-userid?userId=${encodeURIComponent(userId)}`)
        .then(res => res.json())
        .then(data => {
            userIdMsg.textContent = data.message;
            userIdMsg.className = data.available ? "msg success" : "msg error";
            isUserIdChecked = data.available;
        })
        .catch(() => {
            userIdMsg.textContent = "중복 체크 오류";
            userIdMsg.className = "msg error";
            isUserIdChecked = false;
        });
}

userIdInput.addEventListener("input", () => {
    isUserIdChecked = false;
    userIdMsg.textContent = "";
    userIdMsg.className = "msg";
});

/* =========================
   비밀번호 유효성
========================= */
pwInput.addEventListener("input", () => {
    const regex =
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,16}$/;

    pwMsg.textContent = regex.test(pwInput.value)
        ? ""
        : "영문/숫자/특수문자 포함 8~16자";
});

/* =========================
   비밀번호 확인
========================= */
pwConfirmInput.addEventListener("input", () => {
    if (!pwConfirmInput.value) {
        pwConfirmMsg.textContent = "";
        return;
    }

    if (pwConfirmInput.value === pwInput.value) {
        pwConfirmMsg.textContent = "✓";
        pwConfirmMsg.className = "icon ok";
    } else {
        pwConfirmMsg.textContent = "✕";
        pwConfirmMsg.className = "icon no";
    }
});

/* =========================
   ✅ 이메일 중복 체크 (추가)
========================= */
emailInput.addEventListener("blur", () => {
    const email = emailInput.value.trim();

    // 이메일 비어있으면 통과
    if (!email) {
        isEmailAvailable = true;
        return;
    }

    fetch(`/api/users/check-email?email=${encodeURIComponent(email)}`)
        .then(res => res.json())
        .then(data => {
            isEmailAvailable = data.available;
        })
        .catch(() => {
            // 에러면 안전하게 막기
            isEmailAvailable = false;
        });
});

/* =========================
   프로필 이름 중복 체크 (debounce)
========================= */
let profileNameTimer = null;

profileNameInput.addEventListener("input", () => {
    const profileNm = profileNameInput.value.trim();

    isProfileNameAvailable = false;
    if (profileNm.length < 2) return;

    clearTimeout(profileNameTimer);

    profileNameTimer = setTimeout(() => {
        fetch(`/api/users/check-profilename?profileNm=${encodeURIComponent(profileNm)}`)
            .then(res => res.json())
            .then(data => {
                profileNameMsg.textContent = data.message;
                profileNameMsg.className = data.available ? "msg success" : "msg error";
                isProfileNameAvailable = data.available;
            })
            .catch(() => {
                profileNameMsg.textContent = "확인 실패";
                profileNameMsg.className = "msg error";
                isProfileNameAvailable = false;
            });
    }, 500);
});

/* =========================
   생년월일 체크
========================= */
const birthdayInput = document.querySelector("input[name='birthday']");
birthdayInput.addEventListener("change", () => {
    if (!birthdayInput.value) return;

    const selectedDate = new Date(birthdayInput.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (selectedDate > today) {
        alert("생년월일은 오늘 이후 날짜로 선택할 수 없어요");
        birthdayInput.value = "";
        setTimeout(() => birthdayInput.focus(), 0);
    }
});

/* =========================
   submit 최종 처리
========================= */
const requiredFields = [
    { el: userIdInput, name: "아이디" },
    { el: pwInput, name: "비밀번호" },
    { el: pwConfirmInput, name: "비밀번호 확인" },
    { el: document.querySelector("input[name='userNm']"), name: "이름" },
    { el: profileNameInput, name: "프로필 이름" },
    { el: document.querySelector("input[name='birthday']"), name: "생년월일" },
    { el: document.querySelector("input[name='phoneNo']"), name: "휴대폰 번호" },
    { el: emailInput, name: "이메일" },
];

form.addEventListener("submit", function (e) {
    e.preventDefault();

    if (!isUserIdChecked) {
        userIdInput.focus();
        return;
    }

    const genderChecked = document.querySelector("input[name='gender']:checked");
    if (!genderChecked) {
        alert("성별을 선택해주세요");
        document.querySelector("input[name='gender']").focus();
        return;
    }

    if (!isProfileNameAvailable) {
        profileNameInput.focus();
        profileNameInput.select();
        return;
    }

    // ✅ 이메일 중복이면 여기서만 차단
    if (emailInput.value.trim() && !isEmailAvailable) {
        alert("이미 사용 중인 이메일이라 회원가입이 안 돼요");
        emailInput.focus();
        return;
    }

    for (let field of requiredFields) {
        if (!field.el || !field.el.value.trim()) {
            field.el.focus();
            return;
        }
    }

    fetch("/users/signup", {
        method: "POST",
        body: new FormData(form)
    })
        .then(res => {
            if (!res.ok) throw new Error();
            return res.text();
        })
        .then(() => {
            alert("회원가입 성공");
            location.href = "/users/signup-extra";
        })
        .catch(() => {});
});
