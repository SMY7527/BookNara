package com.booknara.booknaraPrj.mypage.mylibrary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller   // ❗ RestController → Controller 로 변경
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final BootpayService bootpayService;

    /**
     * 1️⃣ 부트페이 결제 완료 후 redirect (GET)
     * redirect_url 로 호출됨
     */
    @GetMapping("/redirect")
    public String redirect(
            @RequestParam(required = false) String receipt_id,
            Model model
    ) {

        if (receipt_id == null) {
            model.addAttribute("success", false);
            model.addAttribute("message", "결제 정보가 없습니다.");
            return "paymentResult";
        }

        // 서버에서 결제 검증
        Map<String, Object> result = bootpayService.verify(receipt_id);

        model.addAttribute("result", result);
        model.addAttribute("success", result.get("success"));

        return "paymentResult"; // templates/paymentResult.html
    }

    /**
     * 2️⃣ JS에서 결제 검증 요청 (POST)
     * fetch("/payment/verify") 로 호출
     */
    @ResponseBody
    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Map<String, String> req) {
        String receiptId = req.get("receiptId");
        return bootpayService.verify(receiptId);
    }
}
