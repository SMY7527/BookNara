/*이벤트 배너 슬라이드*/
document.addEventListener("DOMContentLoaded", () => {
  const track = document.querySelector(".banner-track");
  const slides = document.querySelectorAll(".banner-slide");
  const dots = document.querySelectorAll(".banner-indicator .dot");
  const prevBtn = document.querySelector(".banner-nav.prev");
  const nextBtn = document.querySelector(".banner-nav.next");

  let currentIndex = 0;
  const slideCount = slides.length;

  function updateSlide(index) {
    track.style.transform = `translateX(-${index * 100}%)`;
    dots.forEach(dot => dot.classList.remove("active"));
    dots[index].classList.add("active");
  }

  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex + 1) % slideCount;
    updateSlide(currentIndex);
  });

  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex - 1 + slideCount) % slideCount;
    updateSlide(currentIndex);
  });

  dots.forEach((dot, index) => {
    dot.addEventListener("click", () => {
      currentIndex = index;
      updateSlide(currentIndex);
    });
  });
});


document.addEventListener("DOMContentLoaded", () => {

  /* ===============================
     1️⃣ 내가 정한 해시태그 9개 (GENRE_ID + 텍스트)
     =============================== */
  const MALLANG_HASHTAGS = [
    { genreId: 1,    label: "#이야기에빠지다" },
    { genreId: 55889, label: "#마음이따뜻해지는" },
    { genreId: 336,  label: "#나를키우는시간" },
    { genreId: 656,  label: "#생각이깊어지는" },
    { genreId: 74,   label: "#세상을읽다" },
    { genreId: 170,  label: "#돈과인생이야기" },
    { genreId: 987,  label: "#호기심폭발" },
    { genreId: 517,  label: "#취향저격" },
    { genreId: 1108, label: "#꿈이자라는" }
  ];

  const hashtagBox = document.getElementById("recommendHashtags");
  const bookCards = document.querySelectorAll(".book-card");

  /* ===============================
     2️⃣ 배열 섞어서 랜덤 3개 뽑기
     =============================== */
  function pickRandom3(arr) {
    return [...arr].sort(() => Math.random() - 0.5).slice(0, 3);
  }

  /* ===============================
     3️⃣ 해시태그 랜덤 3개 렌더링
     =============================== */
  function renderHashtags() {
    const picked = pickRandom3(MALLANG_HASHTAGS);
    hashtagBox.innerHTML = "";

    picked.forEach((tag, idx) => {
      const btn = document.createElement("button");
      btn.className = "hashtag";
      btn.innerText = tag.label;
      btn.dataset.genreId = tag.genreId;

      btn.addEventListener("click", () => {
        document.querySelectorAll(".hashtag").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        loadBooks(tag.genreId);
      });

      hashtagBox.appendChild(btn);

      // 첫 해시태그 자동 선택
      if (idx === 0) {
        btn.classList.add("active");
        loadBooks(tag.genreId);
      }
    });
  }

  /* ===============================
     4️⃣ 해시태그 클릭 → DB에서 랜덤 3권
     =============================== */
  function loadBooks(genreId) {
    fetch(`/api/main/mallang-pick/books?genreId=${genreId}`)
      .then(res => res.json())
      .then(books => {
        bookCards.forEach((card, idx) => {
          const book = books[idx];

          if (!book) {
            card.querySelector("img").src = "/images/placeholder_book.png";
            card.querySelector(".book-title").innerText = "추천 도서 준비 중";
            card.querySelector(".book-author").innerText = "";
            return;
          }

          card.querySelector("img").src = book.bookImg;
          card.querySelector("img").alt = book.bookTitle;
          card.querySelector(".book-title").innerText = book.bookTitle;
          card.querySelector(".book-author").innerText =
            `${book.authors} · ${book.publisher}`;
        });
      });
  }

  /* ===============================
     5️⃣ 시작
     =============================== */
  renderHashtags();

});


//사서추천
// 사서 추천
document.addEventListener("DOMContentLoaded", () => {

  const list = document.getElementById("librarianBookList"); // 컨테이너

  fetch("/api/main/librarian/books")
    .then(res => res.json())
    .then(books => {
      list.innerHTML = ""; // 기존 더미 제거

      books.forEach(book => {
        const card = document.createElement("div");
        card.className = "librarian-book-card";

        card.innerHTML = `
          <div class="book-cover">
            <img src="${book.bookImg}" alt="${book.bookTitle}">
          </div>
          <div class="book-info">
            <p class="book-title">${book.bookTitle}</p>
            <p class="book-author">
              ${book.authors} · ${book.publisher}
            </p>
          </div>
        `;

        list.appendChild(card);
      });
    })
    .catch(err => console.error("사서 추천 로딩 실패", err));
});


// 신간도서
document.addEventListener("DOMContentLoaded", () => {

  const list = document.getElementById("newBooksList");
  if (!list) return;

  fetch("/api/main/new-books")
    .then(res => res.json())
    .then(books => {
      list.innerHTML = "";

      books.forEach(book => {
        const card = document.createElement("div");
        card.className = "librarian-book-card";

        card.innerHTML = `
          <a href="/books/${book.isbn13}" class="new-book-link">
            <div class="book-cover">
              <img src="${book.bookImg}" alt="${book.bookTitle}">
            </div>
            <div class="book-info">
              <p class="book-title">${book.bookTitle}</p>
              <p class="book-author">
                ${book.authors} · ${book.publisher}
              </p>
            </div>
          </a>
        `;

        list.appendChild(card);
      });
    })
    .catch(err => console.error("신간도서 로딩 실패", err));
});

