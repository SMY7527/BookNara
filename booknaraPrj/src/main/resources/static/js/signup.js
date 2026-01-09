const userIdInput = document.getElementById("userIdInput");
const userIdMsg = document.getElementById("userIdMsg");

const pwInput = document.getElementById("password");
const pwMsg = document.getElementById("pwMsg");

const pwConfirmInput = document.getElementById("passwordConfirm");
const pwConfirmMsg = document.getElementById("pwConfirmMsg");

const profileNameInput = document.getElementById("profileNameInput");
const profileNameMsg = document.getElementById("profileNameMsg");

const emailInput = document.querySelector("input[name='email']");
const birthdayInput = document.querySelector("input[name='birthday']");
const phoneInput = document.querySelector("input[name='phoneNo']");
const nameInput = document.querySelector("input[name='userNm']");

const form = document.querySelector("form");

/* =========================
   상태값
========================= */
let isUserIdChecked = false;
let isPasswordValid = false;
let isPasswordMatched = false;
let isProfileNameAvailable = false;
let isEmailAvailable = true;

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

  isPasswordValid = regex.test(pwInput.value);
  pwMsg.textContent = isPasswordValid
    ? ""
    : "영문/숫자/특수문자 포함 8~16자";
});

/* =========================
   비밀번호 확인
========================= */
pwConfirmInput.addEventListener("input", () => {
  if (!pwConfirmInput.value) {
    pwConfirmMsg.textContent = "";
    isPasswordMatched = false;
    return;
  }

  isPasswordMatched = pwConfirmInput.value === pwInput.value;
  pwConfirmMsg.textContent = isPasswordMatched ? "✓" : "✕";
  pwConfirmMsg.className = isPasswordMatched ? "icon ok" : "icon no";
});

/* =========================
   프로필 이름 중복 체크 (debounce)
========================= */
let profileTimer = null;

profileNameInput.addEventListener("input", () => {
  const profileNm = profileNameInput.value.trim();
  isProfileNameAvailable = false;

  if (profileNm.length < 2) return;

  clearTimeout(profileTimer);
  profileTimer = setTimeout(() => {
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
   이메일 중복 체크
========================= */
emailInput.addEventListener("blur", () => {
  const email = emailInput.value.trim();
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
      isEmailAvailable = false;
    });
});

/* =========================
   생년월일 체크
========================= */
birthdayInput.addEventListener("change", () => {
  if (!birthdayInput.value) return;

  const selected = new Date(birthdayInput.value);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (selected > today) {
    alert("생년월일은 오늘 이후 날짜로 선택할 수 없어요");
    birthdayInput.value = "";
    birthdayInput.focus();
  }
});

/* =========================
   submit 최종 검증 (순서 고정)
========================= */
form.addEventListener("submit", (e) => {
  e.preventDefault();

  // 1️⃣ 필수 입력값 (위 → 아래)
  const required = [
    { el: userIdInput, msg: "아이디를 입력해주세요" },
    { el: pwInput, msg: "비밀번호를 입력해주세요" },
    { el: pwConfirmInput, msg: "비밀번호 확인을 입력해주세요" },
    { el: nameInput, msg: "이름을 입력해주세요" },
    { el: profileNameInput, msg: "프로필 이름을 입력해주세요" },
    { el: birthdayInput, msg: "생년월일을 선택해주세요" },
    { el: phoneInput, msg: "휴대폰 번호를 입력해주세요" },
    { el: emailInput, msg: "이메일을 입력해주세요" },
  ];

  for (let f of required) {
    if (!f.el.value.trim()) {
      alert(f.msg);
      f.el.focus();
      return;
    }
  }

  // 2️⃣ 아이디 중복
  if (!isUserIdChecked) {
    alert("아이디 중복 확인을 해주세요");
    userIdInput.focus();
    return;
  }

  // 3️⃣ 비밀번호 형식
  if (!isPasswordValid) {
    alert("비밀번호 형식이 맞지 않아요");
    pwInput.focus();
    return;
  }

  // 4️⃣ 비밀번호 일치
  if (!isPasswordMatched) {
    alert("비밀번호가 서로 일치하지 않아요");
    pwConfirmInput.focus();
    return;
  }

  // 5️⃣ 성별
  const gender = document.querySelector("input[name='gender']:checked");
  if (!gender) {
    alert("성별을 선택해주세요");
    document.querySelector("input[name='gender']").focus();
    return;
  }

  // 6️⃣ 프로필 이름 중복
  if (!isProfileNameAvailable) {
    alert("프로필 이름 중복 확인이 필요해요");
    profileNameInput.focus();
    profileNameInput.select();
    return;
  }

  // 7️⃣ 이메일 중복
  if (!isEmailAvailable) {
    alert("이미 등록된 이메일입니다");
    emailInput.focus();
    return;
  }

  // 8️⃣ 전송
  fetch("/users/signup", {
    method: "POST",
    body: new FormData(form)
  })
    .then(res => {
      if (!res.ok) throw new Error();
      return res.text();
    })
    .then(() => {
      location.href = "/users/signup-extra";
    });
});
