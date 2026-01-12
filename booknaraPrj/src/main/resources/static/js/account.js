document.addEventListener("DOMContentLoaded", () => {

  /* =========================
     요소 가져오기 (있을 때만 사용)
  ========================= */
  const tabFindId = document.getElementById("tabFindId");
  const tabFindPw = document.getElementById("tabFindPw");
  const findIdSection = document.getElementById("findIdSection");
  const findPwSection = document.getElementById("findPwSection");

  const findIdBtn = document.getElementById("findIdBtn");
  const findIdName = document.getElementById("findIdName");
  const findIdEmail = document.getElementById("findIdEmail");
  const findIdMsg = document.getElementById("findIdMsg");

  const sendCodeBtn = document.getElementById("sendCodeBtn");
  const findPwUserId = document.getElementById("findPwUserId");
  const findPwEmail = document.getElementById("findPwEmail");
  const sendCodeMsg = document.getElementById("sendCodeMsg");
  const pwStep2 = document.getElementById("pwStep2");

  const verifyCodeBtn = document.getElementById("verifyCodeBtn");
  const verifyCode = document.getElementById("verifyCode");
  const verifyMsg = document.getElementById("verifyMsg");

  const resetPwBtn = document.getElementById("resetPwBtn");
  const newPassword = document.getElementById("newPassword");
  const newPasswordConfirm = document.getElementById("newPasswordConfirm");
  const resetPwMsg = document.getElementById("resetPwMsg");


  let timerInterval;
  let remain = 300; // 5분

  function startTimer() {
    clearInterval(timerInterval);
    remain = 300;

    const timerEl = document.getElementById("timer");
    timerEl.style.display = "block";

    timerInterval = setInterval(() => {
      const min = String(Math.floor(remain / 60)).padStart(2, "0");
      const sec = String(remain % 60).padStart(2, "0");
      timerEl.textContent = `${min}:${sec}`;

      if (remain <= 0) {
        clearInterval(timerInterval);
        timerEl.textContent = "인증시간 만료";
        document.getElementById("sendCodeBtn").disabled = false;
      }

      remain--;
    }, 1000);
  }

  /* =========================
     탭 전환
  ========================= */
  if (tabFindId && tabFindPw && findIdSection && findPwSection) {
    tabFindId.addEventListener("click", () => {
      findIdSection.style.display = "block";
      findPwSection.style.display = "none";
      tabFindId.classList.add("active");
      tabFindPw.classList.remove("active");
    });

    tabFindPw.addEventListener("click", () => {
      findIdSection.style.display = "none";
      findPwSection.style.display = "block";
      tabFindPw.classList.add("active");
      tabFindId.classList.remove("active");
    });
  }

  /* =========================
     아이디 찾기
  ========================= */
  if (findIdBtn && findIdName && findIdEmail && findIdMsg) {
    findIdBtn.addEventListener("click", () => {
      const name = findIdName.value.trim();
      const email = findIdEmail.value.trim();

      if (!name || !email) {
        findIdMsg.textContent = "이름이랑 이메일 다 입력해주세요";
        findIdMsg.className = "msg";
        return;
      }

      fetch("/users/find-id", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email })
      })
        .then(res => res.json())
        .then(data => {
          findIdMsg.textContent = data.message;
          findIdMsg.className = data.success ? "msg success" : "msg";
        })
        .catch(() => {
          findIdMsg.textContent = "요청 실패";
          findIdMsg.className = "msg";
        });
    });
  }

  /* =========================
     비밀번호 찾기 - 1단계: 인증코드 보내기
  ========================= */
  if (sendCodeBtn && findPwUserId && findPwEmail && sendCodeMsg && pwStep2) {
    sendCodeBtn.addEventListener("click", () => {
      const userId = findPwUserId.value.trim();
      const email = findPwEmail.value.trim();

      if (!userId || !email) {
        sendCodeMsg.textContent = "아이디랑 이메일 모두 입력해주세요";
        sendCodeMsg.className = "msg";
        return;
      }

      fetch("/users/find-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, email })
      })
        .then(res => res.json())
        .then(data => {
          sendCodeMsg.textContent = data.message;
          sendCodeMsg.className = data.success ? "msg success" : "msg";
          if (data.success) pwStep2.style.display = "block";
          startTimer();
        })
        .catch(() => {
          sendCodeMsg.textContent = "정보를 찾을 수 없습니다";
          sendCodeMsg.className = "msg";
        });
    });
  }

  /* =========================
     비밀번호 찾기 - 2단계: 인증 확인 → 재설정 페이지로 이동
  ========================= */
  if (verifyCodeBtn && verifyCode && verifyMsg) {
    verifyCodeBtn.addEventListener("click", () => {
      const code = verifyCode.value.trim();

      if (!code) {
        verifyMsg.textContent = "인증코드 입력하세요";
        verifyMsg.className = "msg";
        return;
      }

      fetch("/users/verify-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code })
      })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            location.href = "/users/reset-password-form";
          } else {
            verifyMsg.textContent = data.message;
            verifyMsg.className = "msg";
          }
        })
        .catch(() => {
          verifyMsg.textContent = "인증번호가 다릅니다";
          verifyMsg.className = "msg";
        });
    });
  }

  /* =========================
     비밀번호 재설정 페이지: 변경
  ========================= */
  if (resetPwBtn && newPassword && newPasswordConfirm && resetPwMsg) {
    resetPwBtn.addEventListener("click", () => {
      const pw = newPassword.value;
      const pw2 = newPasswordConfirm.value;

      if (!pw || !pw2) {
        resetPwMsg.textContent = "영문/숫자/특수문자 포함 8~16자";
        resetPwMsg.className = "msg";
        return;
      }

      if (pw !== pw2) {
        resetPwMsg.textContent = "비밀번호가 다릅니다";
        resetPwMsg.className = "msg";
        return;
      }

      fetch("/users/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: pw })
      })
        .then(res => res.json())
        .then(data => {
          resetPwMsg.textContent = data.message;
          resetPwMsg.className = data.success ? "msg success" : "msg";

          if (data.success) {
            alert("비밀번호 변경 완료");
            location.href = "/users/login";
          }
        })
//        .catch(() => {
//          resetPwMsg.textContent = "올바른 형식이 아닙니다";
//          resetPwMsg.className = "msg";
//        });
    });
  }

});
