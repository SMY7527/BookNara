package com.booknara.booknaraPrj.bookcart.controller;

import com.booknara.booknaraPrj.bookcart.service.BookCartService;
import com.booknara.booknaraPrj.bookcart.service.OrderPaymentService;
import com.booknara.booknaraPrj.mypage.mylibrary.BootpayService;
import com.booknara.booknaraPrj.mypage.mylibrary.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/book/order")
public class BookOrderController {

    private final BookCartService cartService;
    private final OrderPaymentService orderPaymentService;
    private final PaymentService paymentService;
    private final BootpayService bootpayService;

    private String requireUserId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return auth.getName();
    }

    /** 주문/결제 페이지 */
    @GetMapping
    public String orderPage(Authentication auth, Model model) {
        String userId = requireUserId(auth);

        var items = cartService.listWithLendable(userId);

        boolean hasPaper = items.stream().anyMatch(it -> !"Y".equalsIgnoreCase(it.getEbookYn()));
        boolean hasUnlendable = items.stream().anyMatch(it -> it.getLendableYn() == null || !it.getLendableYn());

        // quota도 같이 내려줘야 화면 경고가 정상
        var quota = cartService.getLendQuota(userId);
        boolean hasQuotaExceeded = quota != null && quota.getAvailableCount() < quota.getCartCount();

        int expectedPrice = hasPaper ? 3000 : 0;

        model.addAttribute("items", items);
        model.addAttribute("quota", quota);

        model.addAttribute("hasPaper", hasPaper);
        model.addAttribute("expectedPrice", expectedPrice);

        model.addAttribute("hasUnlendable", hasUnlendable);
        model.addAttribute("hasQuotaExceeded", hasQuotaExceeded);

        model.addAttribute("step", "PAY");
        return "bookcart/order";
    }

    /**
     * ✅ 결제 버튼 누르기 직전, 서버가 "최종 판정" 내려줌
     * - 프론트가 hasPaper를 잘못 계산해도 서버가 맞춰줌
     */
    @GetMapping("/precheck")
    @ResponseBody
    public Map<String, Object> precheck(Authentication auth) {
        String userId = requireUserId(auth);

        var items = cartService.listWithLendable(userId);
        if (items == null || items.isEmpty()) {
            return Map.of("ok", false, "code", "EMPTY_CART", "message", "장바구니가 비어있습니다.");
        }

        boolean hasPaper = items.stream().anyMatch(it -> !"Y".equalsIgnoreCase(it.getEbookYn()));
        boolean hasUnlendable = items.stream().anyMatch(it -> it.getLendableYn() == null || !it.getLendableYn());

        var quota = cartService.getLendQuota(userId);
        boolean hasQuotaExceeded = quota != null && quota.getAvailableCount() < quota.getCartCount();

        if (hasUnlendable) {
            return Map.of("ok", false, "code", "UNLENDABLE", "message", "대여가 불가능한 도서가 포함되어있습니다.");
        }
        if (hasQuotaExceeded) {
            return Map.of("ok", false, "code", "QUOTA_EXCEEDED", "message", "대여 가능 권수를 초과했습니다.");
        }

        int expectedPrice = hasPaper ? 3000 : 0;

        // ✅ 서버가 최종적으로 "무료/유료" 판단 내려줌
        return Map.of(
                "ok", true,
                "hasPaper", hasPaper,
                "expectedPrice", expectedPrice
        );
    }

    /**
     * ✅ 전자책-only 무료 확정
     * - 프론트가 잘못 호출해도 서버가 409로 깔끔하게 응답
     */
    @PostMapping("/confirm-free")
    @ResponseBody
    public Map<String, Object> confirmFree(Authentication auth) {
        String userId = requireUserId(auth);

        try {
            orderPaymentService.confirmFreeOrder(userId);
            return Map.of("ok", true);
        } catch (IllegalStateException e) {
            // 정책 위반은 409(충돌) or 400(요청 오류) 중 하나인데, "상태 충돌"이라 409 추천
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * ✅ 유료 결제 확정
     * - receiptId 검증 → PAYMENTS 저장 → LENDS 생성(트랜잭션) → CART clear
     */
    @PostMapping("/confirm-paid")
    @ResponseBody
    public Map<String, Object> confirmPaid(@RequestBody Map<String, String> body, Authentication auth) {
        String userId = requireUserId(auth);

        String receiptId = (body == null) ? null : body.get("receiptId");
        if (receiptId == null || receiptId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receiptId required");
        }

        // 1) 부트페이 영수증 검증
        Map<String, Object> verifyResult = bootpayService.verify(receiptId);
        boolean success = Boolean.TRUE.equals(verifyResult.get("success"));
        if (!success) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verify failed");
        }

        try {
            // 2) 결제 저장 + 3) 대여 확정
            // (※ 이상적으로는 이 두 작업을 하나의 @Transactional로 묶는 게 베스트지만,
            //  지금은 PaymentService 수정 제한이 있으니 컨트롤러에서 순차 실행)
            paymentService.savePayment(userId, receiptId, verifyResult);
            orderPaymentService.confirmPaidOrder(userId, receiptId);

            return Map.of("ok", true);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * ✅ ResponseStatusException이든 뭐든 "trace"가 alert에 뜨지 않게
     * 항상 {ok:false, message:"..."} 형태로 내려주는 공통 핸들러
     */
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    @ResponseStatus
    public Map<String, Object> handleRse(ResponseStatusException e) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("status", e.getStatusCode().value());
        res.put("message", e.getReason() == null ? "요청 처리 실패" : e.getReason());
        return res;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIse(IllegalStateException e) {
        return Map.of("ok", false, "status", 409, "message", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleEtc(Exception e) {
        // 운영에서는 message를 일반화하는게 맞지만, 지금은 디버깅 단계니까 최소한만 노출
        return Map.of("ok", false, "status", 500, "message", "서버 오류가 발생했습니다.");
    }
}
