package com.booknara.booknaraPrj.mypage.mylibrary;

import com.booknara.booknaraPrj.mypage.mylibrary.PaymentDto;
import com.booknara.booknaraPrj.mypage.mylibrary.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;

    @Transactional
    public void savePayment(
            String userId,
            String receiptId,
            Map<String, Object> verifyResult
    ) {

        System.out.println("🔥 [SAVE PAYMENT] userId=" + userId + ", receiptId=" + receiptId);


        PaymentDto dto = new PaymentDto();

        dto.setUserId(userId);          // ✅ DB 기준 USER_ID
        dto.setReceiptId(receiptId);    // null 가능
        dto.setStatus("PAID");          // ❗ 무조건 PAID
        dto.setPaidAt(LocalDateTime.now());

        // (선택) 로그용 데이터 저장하고 싶으면
        // dto.setVerifyJson(new ObjectMapper().writeValueAsString(verifyResult));

        paymentMapper.insertPayment(dto);
    }

}
