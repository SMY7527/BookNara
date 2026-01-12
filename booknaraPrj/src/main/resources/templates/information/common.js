(() => {
  const NAV = [
    { href: "/event-page/event.html", label: "이벤트" },
    { href: "/notice-page/notice.html", label: "공지사항" },
    { href: "/guide-page/guide.html", label: "이용안내" },
    { href: "/FAQ/faq.html", label: "FAQ" },
    { href: "/inquiry-page/inquiry.html", label: "1:1문의" },
  ];

  function currentFile() {
    const p = location.pathname.split("/").pop();
    return p && p.length ? p : "index.html";
  }

  function renderHeader() {
    // 페이지에 id="siteHeader" 있으면 거기에 넣고, 없으면 기존 .site-header 사용
    const header =
      document.getElementById("siteHeader") ||
      document.querySelector(".site-header");
    if (!header) return;

    header.classList.add("site-header");
    header.innerHTML = `
      <div class="header-inner">
        <a class="brand" href="index.html" aria-label="말랑 홈">
          <span class="brand-badge">말</span>
          <span class="brand-name">말랑 고객센터</span>
        </a>

        <nav class="gnb" aria-label="주 메뉴">
          ${NAV.map((n) => `<a href="${n.href}">${n.label}</a>`).join("")}
        </nav>
      </div>
    `;

    const cur = currentFile();
    header.querySelectorAll(".gnb a").forEach((a) => {
      if (a.getAttribute("href") === cur) a.classList.add("active");
    });
  }

  function ensureToastRoot() {
    let root = document.getElementById("toastRoot");
    if (root) return root;
    root = document.createElement("div");
    root.id = "toastRoot";
    root.className = "toast-root";
    document.body.appendChild(root);
    return root;
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, (m) => {
      const map = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      };
      return map[m];
    });
  }

  // 전역으로 사용: toast("메시지"), toast("성공", "success")
  window.toast = (message, type = "info") => {
    const root = ensureToastRoot();
    const el = document.createElement("div");
    el.className = `toast toast-${type}`;
    el.setAttribute("role", "status");

    el.innerHTML = `
      <div class="toast-msg">${escapeHtml(message)}</div>
      <button class="toast-x" type="button" aria-label="닫기">✕</button>
    `;

    root.appendChild(el);

    const remove = () => {
      el.classList.add("out");
      el.addEventListener("animationend", () => el.remove(), { once: true });
    };

    el.querySelector(".toast-x").addEventListener("click", remove);
    setTimeout(remove, 2600);
  };

  document.addEventListener("DOMContentLoaded", () => {
    renderHeader();
    ensureToastRoot();
  });
})();
