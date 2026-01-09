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

  /* ===== 장르별 도서 데이터 (나중에 DB로 교체) ===== */
  const bookData = {
    warm: [
      { title: "너를 생각하는 것이 나의 일생이었다", author: "정채봉", img: "./images/book1.jpg" },
      { title: "오늘도 고마워", author: "김신지", img: "./images/book2.jpg" },
      { title: "괜찮아, 잘 될 거야", author: "에세이", img: "./images/book3.jpg" }
    ],
    mystery: [
      { title: "용의자 X의 헌신", author: "히가시노 게이고", img: "./images/book4.png" },
      { title: "백야행", author: "히가시노 게이고", img: "./images/book5.png" },
      { title: "셜록 홈즈", author: "아서 코난 도일", img: "./images/book6.png" }
    ],
    horror: [
      { title: "사랑해도 혼나지 않는 꿈이었다", author: "시오엔", img: "./images/book7.png" },
      { title: "기담", author: "공포 단편", img: "./images/book8.png" },
      { title: "소름", author: "미스터리", img: "./images/book9.png" }
    ]
  };

  /* ===== 기존 DOM 요소 ===== */
  const hashtags = Array.from(document.querySelectorAll(".hashtag"));
  const cards = document.querySelectorAll(".book-card");

  /* ===== 해시태그 10개 중 랜덤 3개만 표시 ===== */
  hashtags.sort(() => Math.random() - 0.5);

  hashtags.forEach((tag, idx) => {
    tag.style.display = idx < 3 ? "block" : "none";
  });

  /* 첫 번째 해시태그 자동 선택 */
  const firstTag = hashtags.find(tag => tag.style.display === "block");
  if (firstTag) {
    firstTag.classList.add("active");
    renderBooks(firstTag.dataset.genre);
  }

  /* ===== 해시태그 클릭 ===== */
  hashtags.forEach(tag => {
    tag.addEventListener("click", () => {
      if (tag.style.display === "none") return;

      hashtags.forEach(t => t.classList.remove("active"));
      tag.classList.add("active");

      renderBooks(tag.dataset.genre);
    });
  });

  /* ===== 카드 내용만 교체 ===== */
  function renderBooks(genre) {
    const books = bookData[genre] || [];

    cards.forEach((card, idx) => {
      const book = books[idx];
      if (!book) return;

      card.querySelector("img").src = book.img;
      card.querySelector("img").alt = book.title;
      card.querySelector(".book-title").innerText = book.title;
      card.querySelector(".book-author").innerText = book.author;
    });
  }

});




document.addEventListener("DOMContentLoaded", () => {
  const track = document.querySelector(".new-books-track");
  const wrapper = document.querySelector(".new-books-track-wrapper");
  const prevBtn = document.querySelector(".new-books-slider .prev");
  const nextBtn = document.querySelector(".new-books-slider .next");

  const cardWidth = 180;
  const gap = 34;
  const step = cardWidth + gap;

  let index = 0;

  function getMaxIndex() {
    const trackWidth = track.scrollWidth;
    const wrapperWidth = wrapper.clientWidth;
    return Math.max(0, Math.ceil((trackWidth - wrapperWidth) / step));
  }

  function update() {
    track.style.transform = `translateX(-${index * step}px)`;
  }

  nextBtn.addEventListener("click", () => {
    const maxIndex = getMaxIndex();
    if (index < maxIndex) {
      index++;
      update();
    }
  });

  prevBtn.addEventListener("click", () => {
    if (index > 0) {
      index--;
      update();
    }
  });
});
