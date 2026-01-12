package com.booknara.booknaraPrj.bookcart.service;

import com.booknara.booknaraPrj.bookcart.mapper.BookCartMapper;
import com.booknara.booknaraPrj.bookcirculation.command.mapper.BookCirculationCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final BookCartService cartService;
    private final BookCartMapper cartMapper;                  // quota + cart clear
    private final BookCirculationCommandMapper lendMapper;    // FOR UPDATE 선점 + LENDS insert

    /** 전자책-only: 무료 확정 */
    @Transactional
    public void confirmFreeOrder(String userId) {
        createLendsFromCart(userId, false, null);
    }

    /** 종이책 포함: 결제 검증 성공 후 확정 */
    @Transactional
    public void confirmPaidOrder(String userId, String receiptId) {
        createLendsFromCart(userId, true, receiptId);
    }

    /**
     * @param expectPaper true면 종이책 포함 주문(유료), false면 전자책-only(무료)
     * @param receiptId 결제 주문이면 멱등성 체크에 사용(선택)
     */
    private void createLendsFromCart(String userId, boolean expectPaper, String receiptId) {
        List<com.booknara.booknaraPrj.bookcart.dto.BookCartDTO> items = cartService.list(userId);

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }

        // ✅ 0) 유저 상태 체크(차단/연체)
        String blockedYn = lendMapper.selectUserBlockedYn(userId);
        if ("Y".equalsIgnoreCase(blockedYn)) {
            throw new IllegalStateException("차단된 회원은 대여할 수 없습니다.");
        }
        String overdueYn = lendMapper.selectUserOverdueYn(userId);
        if ("Y".equalsIgnoreCase(overdueYn)) {
            throw new IllegalStateException("연체 중인 회원은 대여할 수 없습니다.");
        }

        // ✅ 1) 종이책 포함 여부 서버 재검증
        boolean hasPaper = items.stream().anyMatch(it -> !"Y".equalsIgnoreCase(it.getEbookYn()));

        if (expectPaper) {
            if (!hasPaper) {
                throw new IllegalStateException("전자책-only 주문은 유료 결제 확정 대상이 아닙니다.");
            }
        } else {
            if (hasPaper) {
                throw new IllegalStateException("종이책 포함 주문은 무료 확정 불가입니다.");
            }
        }

        // ✅ 2) quota 체크 (최대 대여 가능 vs 현재 대여중 + 이번 카트)
        Integer maxObj = cartMapper.selectMaxLendCount();
        int max = (maxObj == null || maxObj <= 0) ? 5 : maxObj;

        int current = cartMapper.countMyActiveLends(userId);
        int cartCnt = items.size();

        if (current + cartCnt > max) {
            throw new IllegalStateException(
                    "대여 가능 권수를 초과했습니다. (현재 " + current + "권, 카트 " + cartCnt + "권, 최대 " + max + "권)"
            );
        }

        // ✅ 3) receiptId 멱등성 체크 (중복 호출 방지)
        // if (receiptId != null && paymentLogMapper.exists(receiptId) > 0) return;

        // ✅ 4) 장바구니 → LENDS 생성 (각 ISBN마다 BOOK_ID 선점 후 insert)
        for (var it : items) {
            String isbn13 = it.getIsbn13();

            // 4-1) 이미 대여중인 동일 ISBN 방지
            if (lendMapper.existsActiveLendByUserAndIsbn(userId, isbn13) > 0) {
                throw new IllegalStateException("이미 대여중인 도서가 포함되어 있습니다. ISBN=" + isbn13);
            }

            // 4-2) BOOK_ID 선점 (동시성 핵심: FOR UPDATE)
            Long bookId = lendMapper.selectAvailableBookIdForUpdate(isbn13);

            //  전자책이 BOOKS 재고 row가 없다면 여기서 null로 떨어짐
            if (bookId == null) {

                throw new IllegalStateException("대여 가능한 재고가 없습니다. ISBN=" + isbn13);
            }

            // 4-3) LENDS insert
            String lendId = UUID.randomUUID().toString().replace("-", "");
            int inserted = lendMapper.insertLend(lendId, bookId, userId, isbn13);
            if (inserted != 1) {
                throw new IllegalStateException("대여 생성에 실패했습니다. ISBN=" + isbn13);
            }
        }

        // ✅ 5) 장바구니 비우기
        cartMapper.deleteAll(userId);

    }
}
