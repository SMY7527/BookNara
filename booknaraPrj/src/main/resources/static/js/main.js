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


/* ===============================
   말랑이픽 (해시태그 + 도서)
   =============================== */
document.addEventListener("DOMContentLoaded", () => {

  const hashtagBox = document.getElementById("recommendHashtags");
  const bookCards = document.querySelectorAll(".book-card");

  // 1️⃣ 해시태그 3개 서버에서 가져오기
  fetch("/api/main/mallang-pick/hashtags")
    .then(res => res.json())
    .then(tags => {
      hashtagBox.innerHTML = "";

      if (!tags || tags.length === 0) {
        hashtagBox.innerHTML = "<p>추천 해시태그 준비 중</p>";
        return;
      }

      tags.forEach((tag, idx) => {
        const btn = document.createElement("button");
        btn.className = "hashtag";
        btn.innerText = tag.label;

        btn.addEventListener("click", () => {
          document
            .querySelectorAll("#recommendHashtags .hashtag")
            .forEach(b => b.classList.remove("active"));

          btn.classList.add("active");
          loadBooks(tag.genreId);
        });

        hashtagBox.appendChild(btn);

        // 첫 번째 해시태그 자동 선택
        if (idx === 0) {
          btn.classList.add("active");
          loadBooks(tag.genreId);
        }
      });
    })
    .catch(err => {
      console.error("말랑이픽 해시태그 로딩 실패", err);
      hashtagBox.innerHTML = "<p>추천 해시태그를 불러오지 못했어요</p>";
    });

  // 2️⃣ 해시태그 클릭 → 도서 3권 로딩
  function loadBooks(genreId) {
    fetch(`/api/main/mallang-pick/books?genreId=${genreId}`)
      .then(res => res.json())
      .then(books => {
        bookCards.forEach((card, idx) => {
          const book = books[idx];

          // 데이터 없을 때
          if (!book) {
            card.querySelector(".book-img").src = "/images/placeholder_book.png";
            card.querySelector(".book-title").innerText = "추천 도서 준비 중";
            card.querySelector(".book-author").innerText = "";
            card.querySelector(".book-link").href = "#";
            return;
          }

          card.querySelector(".book-img").src = book.bookImg;
          card.querySelector(".book-img").alt = book.bookTitle;
          card.querySelector(".book-title").innerText = book.bookTitle;
          card.querySelector(".book-author").innerText =
            `${book.authors} · ${book.publisher}`;

          // ⭐ 상세 페이지 이동
          card.querySelector(".book-link").href =
            `/book/detail/${book.isbn13}`;
        });
      })
      .catch(err => {
        console.error("말랑이픽 도서 로딩 실패", err);
      });
  }
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
        const card = document.createElement("a");
        card.className = "librarian-book-card";
        card.href = `/book/detail/${book.isbn13}`;
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
          <a href="/book/detail/${book.isbn13}" class="new-book-link">
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

