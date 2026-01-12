document.addEventListener("DOMContentLoaded", () => {
    const csrf = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".bm-btn");
        if (!btn) return;

        const isbn13 = btn.dataset.isbn;
        if (!isbn13) return;

        try {
            const headers = { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" };
            if (csrf && csrfHeader) headers[csrfHeader] = csrf;

            const res = await fetch("/my/bookmark/toggle", {
                method: "POST",
                headers,
                body: `isbn13=${encodeURIComponent(isbn13)}`
            });

            const data = await res.json();

            if (!data.success) {
                alert(data.message || "처리 실패");
                return;
            }

            // 북마크 탭에서는 N이 되면 즉시 화면에서 제거
            if (data.bookmarkYn === "N") {
                btn.closest(".book-card")?.remove();
            }
        } catch (err) {
            console.error(err);
            alert("서버 통신 오류");
        }
    });
});
