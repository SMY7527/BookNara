/* [1] 평점 섹션 로직 */
  const track = document.getElementById("sliderTrack");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  let index = 0;
  const maxIndex = 6; // 10권 - 4권 노출

  function updateSlider() {
    track.style.transform = `translateX(-${index * (260 + 24)}px)`;
    prevBtn.disabled = index === 0;
    nextBtn.disabled = index === maxIndex;
  }
  prevBtn.onclick = () => { if (index > 0) { index--; updateSlider(); }};
  nextBtn.onclick = () => { if (index < maxIndex) { index++; updateSlider(); }};
  updateSlider();

  /* [2] ⭐ 연령대/성별 섹션 로직 (최종 완성) */
  const ageData = [
    { age: "10대 남성", books: ["정의란 무엇인가", "사피엔스", "군주론", "죽음의 수용소에서", "차라투스트라", "인문학"] },
    { age: "10대 여성", books: ["데미안", "어린왕자", "호밀밭의 파수꾼", "참을 수 없는", "채식주의자", "소년이 온다"] },
    { age: "20대 남성", books: ["정의란 무엇인가", "사피엔스", "군주론", "죽음의 수용소에서", "차라투스트라", "인문학"] },
    { age: "20대 여성", books: ["달러구트", "불편한 편의점", "미드나잇", "우리가 빛의", "시선으로부터", "물고기는"] },
    { age: "30대 남성", books: ["머니 트렌드", "역행자", "부의 시나리오", "타이탄의 도구들", "지적 대화", "생각에 관한"] },
    { age: "30대 여성", books: ["보건교사", "친밀한 이방인", "밝은 밤", "가재가 노래하는", "쇼코의 미소", "오늘 밤"] },
    { age: "40대 남성", books: ["총 균 쇠", "코스모스", "명상록", "손자병법", "이기적 유전자", "역사의 쓸모"] },
    { age: "40대 여성", books: ["마흔에 읽는 니체", "모든 삶은 흐른다", "인생의 기술", "파친코", "아몬드", "어떤 죽음이"] },
    { age: "50대 남성", books: ["백년을 살아보니", "징비록", "서양미술사", "삼국지", "로마인 이야기", "칼의 노래"] },
    { age: "50대 여성", books: ["토지", "혼불", "태백산맥", "나의 문화유산", "장자", "논어"] },
    { age: "60대 남성", books: ["그리스 로마", "목민심서", "난중일기", "대한민국 100년", "세계사 편력", "국가란 무엇인가"] },
    { age: "60대 여성", books: ["시집", "수필집", "성경", "불교 성전", "정원 가꾸기", "건강 백과"] }
  ];

  const ageTrack = document.getElementById('ageTrack');
  const ageDotsContainer = document.getElementById('ageDots');
  const agePrevBtn = document.getElementById('agePrevBtn');
  const ageNextBtn = document.getElementById('ageNextBtn');

  let currentIdx = 2; // 20대 남성 초기 설정

  function initAgeSlider() {
    ageData.forEach((item, i) => {
      // 카드 생성
      const card = document.createElement('div');
      card.className = `age-card`;
      card.innerHTML = `<h3>${item.age}</h3><ul>${item.books.map(b => `<li>${b}</li>`).join('')}</ul>`;
      ageTrack.appendChild(card);

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

    // ⭐ 중앙 정렬 계산 공식
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

  initAgeSlider();