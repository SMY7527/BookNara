document.addEventListener("DOMContentLoaded", () => {

  const alarmBtn = document.getElementById("alarmBtn");
  const alarmBox = document.getElementById("alarmBox");

  if (alarmBtn && alarmBox) {
    alarmBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      alarmBox.style.display =
        alarmBox.style.display === "block" ? "none" : "block";
    });

    document.addEventListener("click", () => {
      alarmBox.style.display = "none";
    });
  }

  const tabs = document.querySelectorAll(".tab");
  const items = document.querySelectorAll(".alarm-item");

  tabs.forEach(tab => {
    tab.addEventListener("click", (e) => {
      e.stopPropagation(); // ⭐ 중요

      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const filter = tab.dataset.filter;

      items.forEach(item => {
        if (filter === "all") {
          item.style.display = "flex";
        } else if (filter === "unread") {
          item.style.display = item.classList.contains("unread") ? "flex" : "none";
        } else {
          item.style.display = item.classList.contains(filter) ? "flex" : "none";
        }
      });
    });
  });

});
