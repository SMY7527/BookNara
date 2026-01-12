document.addEventListener("DOMContentLoaded", () => {

  const track = document.getElementById("sliderTrack");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");

  let index = 0;
  let maxIndex = 0;

/* =========================
   최근 본 책 사이드바
========================= */

function renderRecentBooks() {
  const container = document.getElementById("recentBookList");
  if (!container) return;

  const recent = JSON.parse(localStorage.getItem("recentBooks")) || [];
  container.innerHTML = "";

  recent.forEach(book => {
    const item = document.createElement("a");
    item.className = "side-item";
    item.href = `/book/detail/${book.isbn13}`;

    item.innerHTML = `
      <img src="${book.bookImg}" alt="최근 본 책">
    `;

    container.appendChild(item);
  });
}

  renderRecentBooks();


  function updateSlider() {
    track.style.transform = `translateX(-${index * (260 + 24)}px)`;
    prevBtn.disabled = index === 0;
    nextBtn.disabled = index === maxIndex;
  }

  prevBtn.onclick = () => {
    if (index > 0) {
      index--;
      updateSlider();
    }
  };

  nextBtn.onclick = () => {
    if (index < maxIndex) {
      index++;
      updateSlider();
    }
  };

  // ⭐ 평점 TOP 도서 불러오기
  fetch("/api/recommend/rating")
    .then(res => res.json())
    .then(books => {

      track.innerHTML = "";
      index = 0;

      books.forEach(book => {
        const card = document.createElement("a");
        card.className = "book-card";
        card.href = `/book/detail/${book.isbn13}`;
        card.innerHTML = `
          <img src="${book.naverImage}"
               alt="${book.bookTitle}"
               title="${book.bookTitle} · 평점 ${book.ratingAvg}">
        `;

        track.appendChild(card);
      });

      //
      const visibleCount = 4;
      maxIndex = Math.max(0, books.length - visibleCount);

      updateSlider(); //
    });

});

  /* [2]  연령대/성별 섹션 로직  */

  const ageTrack = document.getElementById('ageTrack');
  const ageDotsContainer = document.getElementById('ageDots');
  const agePrevBtn = document.getElementById('agePrevBtn');
  const ageNextBtn = document.getElementById('ageNextBtn');

  let ageData = [];
  let currentIdx = 2; // 20대 남성 초기 설정


  function initAgeSlider() {
    ageData.forEach((item, i) => {
      // 카드 생성
      const card = document.createElement('div');
      card.className = `age-card`;
card.innerHTML = `
  <h3>${item.age}</h3>
  <ul>
    ${item.books.map(book => `
      <li>
        <a href="/book/detail/${book.isbn}">
          ${book.title}
        </a>
      </li>
    `).join("")}
  </ul>
`;      ageTrack.appendChild(card);
console.log(ageData);
      // 점 생성
      const dot = document.createElement('div');
      dot.className = `dot`;
      dot.onclick = () => moveAge(i);
      ageDotsContainer.appendChild(dot);
    });
    moveAge(currentIdx);
  }

  function moveAge(index) {
    currentIdx = index;
    const cards = document.querySelectorAll('.age-card');
    const dots = document.querySelectorAll('.dot');

    const cardWidth = 260; // 카드(230) + 마진(15*2)
    const viewportWidth = 1300;

    // 중앙 정렬 계산 공식
    // 뷰포트 절반(650)에서 카드 절반(130)을 뺀 위치가 "0번 카드가 중앙에 올 때"의 기준점입니다.
    const centerOffset = (viewportWidth / 2) - (cardWidth / 2);
    const moveX = centerOffset - (index * cardWidth);

    ageTrack.style.transform = `translateX(${moveX}px)`;

    // 강조 효과 및 점 업데이트
    cards.forEach((card, i) => {
      card.classList.toggle('active', i === index);
      dots[i].classList.toggle('active', i === index);
    });

    // 버튼 비활성화 시각화
    agePrevBtn.disabled = index === 0;
    ageNextBtn.disabled = index === ageData.length - 1;
  }

  agePrevBtn.onclick = () => { if(currentIdx > 0) moveAge(currentIdx - 1); };
  ageNextBtn.onclick = () => { if(currentIdx < ageData.length - 1) moveAge(currentIdx + 1); };

  fetch("/api/recommend/age-gender")
    .then(res => res.json())
    .then(res => {
      ageData = res.data;      //  전역 변수에 주입
      initAgeSlider();         //  데이터 받은 뒤 슬라이더 초기화
    })
    .catch(err => {
      console.error("연령대/성별 추천 불러오기 실패", err);
    });


    /*대여순*/
    fetch("/api/recommend/rental")
      .then(res => res.json())
      .then(books => {
        const list = document.getElementById("rentalList");
        list.innerHTML = "";

        books.forEach(book => {
          const item = document.createElement("a");
          item.href = `/book/detail/${book.isbn13}`;
          item.className = "rental-item";

          item.innerHTML = `
            <div class="book-cover">
              <img src="${book.bookImg}" alt="${book.bookTitle}">
            </div>
            <div class="book-info">
              <p class="title">${book.bookTitle}</p>
              <p class="author">${book.authors}·${book.publisher}</p>
            </div>
          `;

          list.appendChild(item);
        });
      });


      /*월간베스트셀러*/
      fetch("/api/recommend/bestseller")
        .then(res => res.json())
        .then(books => {
          const list = document.getElementById("bestsellerList");
          list.innerHTML = "";

          books.forEach(book => {
            const item = document.createElement("a");
            item.href = `/book/detail/${book.isbn13}`;
            item.className = "rental-item"; //  통일

            item.innerHTML = `
              <div class="book-cover">
                <img src="${book.bookImg}" alt="${book.bookTitle}">
              </div>
              <div class="book-info">
                <p class="title">${book.bookTitle}</p>
                <p class="author">${book.authors} · ${book.publisher}</p>
              </div>
            `;

            list.appendChild(item);
          });
        });

