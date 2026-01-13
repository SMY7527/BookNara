const KEY = "INQUIRIES_V5_PRIVATE_PASSWORD";

/* ====== DOM ====== */
const listEl = document.getElementById("inquiryList");
const pagingEl = document.getElementById("paging");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");

const openModalBtn = document.getElementById("openModalBtn");
const closeModalBtn = document.getElementById("closeModalBtn");
const cancelBtn = document.getElementById("cancelBtn");
const modal = document.getElementById("modal");
const modalBackdrop = document.getElementById("modalBackdrop");

const form = document.getElementById("form");
const categoryEl = document.getElementById("category");
const writerEl = document.getElementById("writer");
const titleEl = document.getElementById("title");
const contentEl = document.getElementById("content");
const secretEl = document.getElementById("secret");

const secretPassWrap = document.getElementById("secretPassWrap");
const secretPassEl = document.getElementById("secretPass");
const secretPass2El = document.getElementById("secretPass2");

const filesEl = document.getElementById("files");
const filePreviewEl = document.getElementById("filePreview");
const formMsgEl = document.getElementById("formMsg");

/* 비공개 열람 모달 */
const passBackdrop = document.getElementById("passBackdrop");
const passModal = document.getElementById("passModal");
const passCloseBtn = document.getElementById("passCloseBtn");
const passCancelBtn = document.getElementById("passCancelBtn");
const passOkBtn = document.getElementById("passOkBtn");
const passInput = document.getElementById("passInput");
const passMsg = document.getElementById("passMsg");

/* ====== STATE ====== */
let keyword = "";
let page = 1;
const pageSize = 5;

// localStorage 용량 고려
const MAX_IMAGE_BYTES = 700 * 1024;

// 첨부 선택 상태
let selectedFiles = []; // { file: File, removed: boolean }

// 비공개 열람 상태(현재 탭/세션에서만 유지)
const unlockedIds = new Set(); // item.id

// 비공개 비번 모달 Promise 처리용
let passResolve = null;
let passTargetItem = null;

/* ====== UTILS ====== */
function nowDate() {
  const d = new Date();
  const yy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yy}-${mm}-${dd}`;
}

function maskName(name) {
  const n = String(name ?? "")
    .trim()
    .replace(/\s+/g, "");
  if (!n) return "익명";
  if (n.length === 1) return n[0] + "*";
  return n[0] + "**";
}

function bytesToText(bytes) {
  if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + "MB";
  if (bytes >= 1024) return Math.round(bytes / 1024) + "KB";
  return bytes + "B";
}

function setMsg(msg) {
  formMsgEl.textContent = msg || "";
}
function setPassMsg(msg) {
  passMsg.textContent = msg || "";
}

function save(data) {
  localStorage.setItem(KEY, JSON.stringify(data));
}

function load() {
  const raw = localStorage.getItem(KEY);
  if (raw) return JSON.parse(raw);

  // seed 예시(비공개는 passHash 들어있음)
  const seed = [
    {
      id: 1,
      category: "배송",
      writer: "김서호",
      title: "배송이 멈춘 것 같아요",
      content: "배송중에서 3일째 변동이 없습니다.",
      secret: false,
      date: "2026-01-02",
      answer: "",
      answeredAt: "",
      attachments: [],
    },
    {
      id: 2,
      category: "결제/환불",
      writer: "김민수",
      title: "환불은 언제 되나요?",
      content: "결제 취소했는데 환불이 아직 안 들어왔어요.",
      secret: true,
      passHash: "", // seed에서는 비워둠(실제로 비공개 테스트하려면 등록으로 만들기)
      date: "2026-01-01",
      answer: "카드사 승인 취소는 영업일 기준 3~7일 정도 소요될 수 있어요.",
      answeredAt: "2026-01-02",
      attachments: [],
    },
  ];
  save(seed);
  return seed;
}

let DATA = load();

// WebCrypto SHA-256
async function sha256Hex(text) {
  const enc = new TextEncoder().encode(text);
  const buf = await crypto.subtle.digest("SHA-256", enc);
  const arr = Array.from(new Uint8Array(buf));
  return arr.map((b) => b.toString(16).padStart(2, "0")).join("");
}

function filtered() {
  const k = keyword.trim();
  if (!k) return DATA;

  return DATA.filter((x) => {
    const blob = `${x.title} ${x.content} ${x.category} ${x.writer}`;
    return blob.includes(k);
  });
}

/* ====== MODAL (등록) ====== */
function openModal() {
  modal.classList.remove("hidden");
  modalBackdrop.classList.remove("hidden");
  setMsg("");
  writerEl.focus();
}
function closeModal() {
  modal.classList.add("hidden");
  modalBackdrop.classList.add("hidden");
  form.reset();
  selectedFiles = [];
  renderFilePreview();
  setMsg("");
  // 비공개 비번 영역 닫기
  secretPassWrap.classList.add("hidden");
}

openModalBtn.addEventListener("click", openModal);
closeModalBtn.addEventListener("click", closeModal);
cancelBtn.addEventListener("click", closeModal);
modalBackdrop.addEventListener("click", closeModal);

// 비공개 체크 → 비번 입력칸 표시/숨김
secretEl.addEventListener("change", () => {
  if (secretEl.checked) {
    secretPassWrap.classList.remove("hidden");
    secretPassEl.focus();
  } else {
    secretPassWrap.classList.add("hidden");
    secretPassEl.value = "";
    secretPass2El.value = "";
  }
});

/* ====== 첨부 미리보기 ====== */
function renderFilePreview() {
  filePreviewEl.innerHTML = "";
  const alive = selectedFiles.filter((x) => !x.removed);
  if (alive.length === 0) return;

  alive.forEach((x, idx) => {
    const file = x.file;
    const isImg = file.type.startsWith("image/");

    const item = document.createElement("div");
    item.className = "preview-item";

    const thumb = document.createElement("div");
    thumb.className = "preview-thumb";

    if (isImg) {
      const img = document.createElement("img");
      img.alt = file.name;
      img.src = URL.createObjectURL(file);
      thumb.appendChild(img);
    } else {
      thumb.textContent = "📎";
      thumb.style.fontSize = "28px";
      thumb.style.fontWeight = "900";
      thumb.style.color = "#6b7280";
    }

    const name = document.createElement("div");
    name.className = "preview-name";
    name.title = file.name;
    name.textContent = `${file.name} (${bytesToText(file.size)})`;

    const actions = document.createElement("div");
    actions.className = "preview-actions";

    const delBtn = document.createElement("button");
    delBtn.type = "button";
    delBtn.className = "small-btn";
    delBtn.textContent = "삭제";
    delBtn.addEventListener("click", () => {
      let count = -1;
      for (const it of selectedFiles) {
        if (!it.removed) count++;
        if (count === idx) {
          it.removed = true;
          break;
        }
      }
      renderFilePreview();
    });

    actions.appendChild(delBtn);
    item.appendChild(thumb);
    item.appendChild(name);
    item.appendChild(actions);
    filePreviewEl.appendChild(item);
  });
}

filesEl.addEventListener("change", () => {
  const files = Array.from(filesEl.files || []);
  files.forEach((f) => selectedFiles.push({ file: f, removed: false }));
  filesEl.value = "";
  renderFilePreview();
});

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error("FileReader error"));
    reader.readAsDataURL(file);
  });
}

async function buildAttachments() {
  setMsg("");
  const alive = selectedFiles.filter((x) => !x.removed).map((x) => x.file);
  if (alive.length === 0) return [];

  const result = [];

  for (const f of alive) {
    const isImg = f.type.startsWith("image/");
    if (isImg) {
      if (f.size > MAX_IMAGE_BYTES) {
        result.push({
          kind: "image_meta",
          name: f.name,
          type: f.type,
          size: f.size,
        });
        continue;
      }
      const dataUrl = await fileToDataUrl(f);
      result.push({
        kind: "image",
        name: f.name,
        type: f.type,
        size: f.size,
        dataUrl,
      });
    } else {
      result.push({
        kind: "file",
        name: f.name,
        type: f.type || "application/octet-stream",
        size: f.size,
      });
    }
  }

  if (result.some((x) => x.kind === "image_meta")) {
    setMsg("일부 이미지는 용량이 커서(저장 제한) 파일명만 저장되었습니다.");
  }
  return result;
}

/* ====== 비공개 열람 모달 ====== */
function openPassModal(item) {
  passTargetItem = item;
  passInput.value = "";
  setPassMsg("");

  passModal.classList.remove("hidden");
  passBackdrop.classList.remove("hidden");
  setTimeout(() => passInput.focus(), 0);

  return new Promise((resolve) => {
    passResolve = resolve;
  });
}

function closePassModal(result) {
  passModal.classList.add("hidden");
  passBackdrop.classList.add("hidden");

  const r = passResolve;
  passResolve = null;

  if (r) r(result === true);
  passTargetItem = null;
}

passCloseBtn.addEventListener("click", () => closePassModal(false));
passCancelBtn.addEventListener("click", () => closePassModal(false));
passBackdrop.addEventListener("click", () => closePassModal(false));

passOkBtn.addEventListener("click", async () => {
  if (!passTargetItem) return;

  const entered = passInput.value;
  if (!entered) {
    setPassMsg("비밀번호를 입력해주세요.");
    return;
  }

  // passHash가 없는 옛 데이터는 잠금 처리 안함(레거시)
  if (!passTargetItem.passHash) {
    closePassModal(true);
    return;
  }

  const enteredHash = await sha256Hex(entered);
  if (enteredHash === passTargetItem.passHash) {
    closePassModal(true);
  } else {
    setPassMsg("비밀번호가 일치하지 않습니다.");
  }
});

passInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") passOkBtn.click();
  if (e.key === "Escape") closePassModal(false);
});

/* ====== 렌더 helpers ====== */
function renderAttachments(attachments) {
  const at = attachments || [];
  if (at.length === 0) return "";

  const chips = at
    .map((a) => {
      if (a.kind === "image" && a.dataUrl) {
        const safeName = a.name.replace(/"/g, "&quot;");
        return `<span class="attach-chip">🖼️ <a href="${a.dataUrl}" target="_blank" rel="noopener">${safeName}</a></span>`;
      }
      if (a.kind === "image_meta") {
        const safeName = a.name.replace(/"/g, "&quot;");
        return `<span class="attach-chip">🖼️ ${safeName} (${bytesToText(
          a.size
        )})</span>`;
      }
      const safeName = a.name.replace(/"/g, "&quot;");
      return `<span class="attach-chip">📎 ${safeName} (${bytesToText(
        a.size
      )})</span>`;
    })
    .join("");

  return `
    <div class="section">
      <div class="section-title">첨부파일</div>
      <div class="attach-list">${chips}</div>
    </div>
  `;
}

function makeItem(x) {
  const li = document.createElement("li");
  li.className = "item";

  const isDone = !!(x.answer && x.answer.trim());
  const stateText = isDone ? "답변완료" : "답변대기";
  const stateCls = isDone ? "done" : "wait";
  const lock = x.secret ? "🔒" : "";
  const masked = maskName(x.writer);

  const dateText =
    isDone && x.answeredAt ? `${x.date} / 답변:${x.answeredAt}` : x.date;

  // ✅ 비공개 + passHash가 있고 아직 unlock 안 된 경우 → 내용/첨부 가리기
  const isLocked = !!(x.secret && x.passHash && !unlockedIds.has(x.id));

  const bodyHtml = isLocked
    ? `
      <div class="section">
        <div class="section-title">문의 내용</div>
        <div class="text">비공개입니다. 비밀번호를 입력해 주세요.</div>
      </div>
      <div class="section">
        <div class="section-title">첨부파일</div>
        <div class="text">비공개입니다.</div>
      </div>
      <div class="section">
        <div class="section-title">답변</div>
        <div class="text">비공개입니다.</div>
      </div>
    `
    : `
      <div class="section">
        <div class="section-title">문의 내용</div>
        <div class="text">${x.content}</div>
      </div>

      ${renderAttachments(x.attachments)}

      <div class="section">
        <div class="section-title">답변</div>
        <div class="text">
          ${
            isDone
              ? x.answer
              : "아직 답변이 등록되지 않았습니다. 빠르게 도와드릴게요!"
          }
        </div>
      </div>
    `;

  li.innerHTML = `
    <button class="item-head" type="button">
      <div class="left">
        <span class="badge">${x.category}</span>
        <span class="title">${lock} ${x.title}</span>
      </div>
      <div class="meta">
        <span class="writer">${masked}</span>
        <span class="state ${stateCls}">${stateText}</span>
        <span>${dateText}</span>
        <span class="chev">⌄</span>
      </div>
    </button>

    <div class="item-body">
      ${bodyHtml}
    </div>
  `;

  li.querySelector(".item-head").addEventListener("click", async () => {
    // 같은 리스트에서 하나만 열리게
    const opened = listEl.querySelector(".item.open");
    if (opened && opened !== li) opened.classList.remove("open");

    // 이미 열려있으면 닫기
    if (li.classList.contains("open")) {
      li.classList.remove("open");
      return;
    }

    // 비공개 + 잠금이면 비번 요구 후 열기
    const locked = !!(x.secret && x.passHash && !unlockedIds.has(x.id));
    if (locked) {
      const ok = await openPassModal(x);
      if (!ok) return;

      unlockedIds.add(x.id);
      // 잠금 해제 후 다시 렌더(내용/첨부/답변 보이게)
      render();
      // 렌더가 새로 되므로 현재 id를 찾아서 열기
      const again = Array.from(listEl.querySelectorAll(".item")).find((it) =>
        it.textContent.includes(x.title)
      );
      // 위 방법이 완벽하진 않지만(동일 제목이 있으면), 데모에서는 충분.
      // 더 정확히 하려면 data-id로 찾는 방식 추가해도 됨.
      if (again) again.classList.add("open");
      return;
    }

    li.classList.add("open");
  });

  return li;
}

/* ====== RENDER ====== */
function render() {
  const data = filtered()
    .slice()
    .sort((a, b) => b.id - a.id);

  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));
  if (page > totalPages) page = totalPages;

  const start = (page - 1) * pageSize;
  const slice = data.slice(start, start + pageSize);

  listEl.innerHTML = "";
  if (slice.length === 0) {
    const empty = document.createElement("li");
    empty.className = "item";
    empty.innerHTML = `
      <button class="item-head" type="button" style="cursor:default;">
        <div class="left">
          <span class="badge">안내</span>
          <span class="title">표시할 문의가 없습니다.</span>
        </div>
        <div class="meta"></div>
      </button>
    `;
    listEl.appendChild(empty);
  } else {
    slice.forEach((x) => listEl.appendChild(makeItem(x)));
  }

  pagingEl.innerHTML = "";
  for (let i = 1; i <= totalPages; i++) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "page-btn" + (i === page ? " active" : "");
    btn.textContent = i;
    btn.addEventListener("click", () => {
      page = i;
      listEl.querySelector(".item.open")?.classList.remove("open");
      render();
    });
    pagingEl.appendChild(btn);
  }
}

/* ====== SEARCH ====== */
function doSearch() {
  keyword = searchInput.value;
  page = 1;
  listEl.querySelector(".item.open")?.classList.remove("open");
  render();
}
searchBtn.addEventListener("click", doSearch);
searchInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") doSearch();
});

/* ====== SUBMIT ====== */
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  setMsg("");

  const writer = String(writerEl.value ?? "").trim();
  const title = titleEl.value.trim();
  const content = contentEl.value.trim();
  const isSecret = !!secretEl.checked;

  if (!writer || !title || !content) {
    setMsg("작성자/제목/내용을 입력해주세요.");
    return;
  }

  // ✅ 비공개면 비밀번호 필수 + 확인
  let passHash = "";
  if (isSecret) {
    const p1 = String(secretPassEl.value ?? "");
    const p2 = String(secretPass2El.value ?? "");
    if (!p1 || !p2) {
      setMsg("비공개 비밀번호를 입력해주세요.");
      return;
    }
    if (p1 !== p2) {
      setMsg("비공개 비밀번호가 일치하지 않습니다.");
      return;
    }
    passHash = await sha256Hex(p1);
  }

  const attachments = await buildAttachments();
  const nextId = DATA.reduce((m, x) => Math.max(m, x.id), 0) + 1;

  DATA.push({
    id: nextId,
    category: categoryEl.value,
    writer,
    title,
    content,
    secret: isSecret,
    passHash: isSecret ? passHash : "",
    date: nowDate(),
    answer: "",
    answeredAt: "",
    attachments,
  });

  try {
    save(DATA);
  } catch (err) {
    setMsg(
      "첨부파일 용량이 커서 저장에 실패했습니다. 이미지 크기를 줄이거나 첨부를 줄여주세요."
    );
    DATA = DATA.filter((x) => x.id !== nextId);
    return;
  }

  closeModal();
  render();
});

/* ====== INIT ====== */
render();
