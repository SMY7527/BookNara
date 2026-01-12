package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final BootpayService bootpayService;
    private final PaymentService paymentService;

    @GetMapping("/redirect")
    public String redirect(
            @RequestParam(required = false) String receipt_id,
            Model model
    ) {

        // ✅ 현재 로그인한 사용자 ID (서버 기준)
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Map<String, Object> verifyResult =
                bootpayService.verify(receipt_id);

        paymentService.savePayment(userId, receipt_id, verifyResult);

        model.addAttribute("success", true);
        model.addAttribute("message", "결제 완료");

        return "paymentResult";
    }

    @PostMapping("/complete")
    @ResponseBody
    public void complete(@RequestBody Map<String, String> body) {

        // ✅ 서버에서 로그인 사용자 ID 획득
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        String receiptId = body.get("receiptId");

        System.out.println("🔥 [PAYMENT COMPLETE HIT] userId=" + userId);

        paymentService.savePayment(userId, receiptId, Map.of());
    }
}

