package com.booknara.booknaraPrj.mypage.mylibrary;


import kr.co.bootpay.pg.Bootpay;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class BootpayService {

    @Value("${bootpay.rest-api-key}")
    private String restApiKey;

    @Value("${bootpay.private-key}")
    private String privateKey;

    public Map<String, Object> verify(String receiptId) {
        try {
            if (receiptId == null || receiptId.isBlank()) {
                return Map.of("success", false, "message", "receiptId 없음");
            }

            Bootpay bootpay = new Bootpay(restApiKey, privateKey);

            HashMap<String, Object> tokenRes = bootpay.getAccessToken();
            // 공식 예제는 error_code로 성공/실패 판단하는 패턴을 많이 씀 :contentReference[oaicite:7]{index=7}
            if (tokenRes.get("error_code") != null) {
                return Map.of("success", false, "message", "토큰 발급 실패", "data", tokenRes);
            }

            HashMap<String, Object> receiptRes = bootpay.getReceipt(receiptId);
            if (receiptRes.get("error_code") != null) {
                return Map.of("success", false, "message", "결제 조회 실패", "data", receiptRes);
            }

            Map<String, Object> data = (Map<String, Object>) receiptRes.get("data");

            // ✅ Bootpay 결제 상태: 1 = 결제 완료 :contentReference[oaicite:8]{index=8}
            int status = data.get("status") instanceof Number ? ((Number) data.get("status")).intValue() : -999;

            // ✅ 금액도 반드시 검증 (위변조 방지) :contentReference[oaicite:9]{index=9}
            double price = data.get("price") instanceof Number ? ((Number) data.get("price")).doubleValue() : -1;

            boolean isPaid = (status == 1) && (price == 3000);

            return Map.of(
                    "success", isPaid,
                    "status", status,
                    "price", price,
                    "data", receiptRes
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

}
