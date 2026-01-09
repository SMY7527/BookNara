package com.booknara.booknaraPrj.login_signup.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SocialUserIdGenerator {

    private static final Random random = new Random();

    public static String generate(String provider, String phoneNumber) {

        // 날짜 (yyyyMMdd)
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 전화번호 뒤 4자리 (없으면 0000)
        String last4 = "0000";
        if (phoneNumber != null && phoneNumber.length() >= 4) {
            last4 = phoneNumber.substring(phoneNumber.length() - 4);
        }

        // 랜덤 2자리 (00~99)
        int suffix = random.nextInt(100);

        return String.format(
                "%s_%s_%s_%02d",
                provider.toUpperCase(),
                date,
                last4,
                suffix
        );
    }
}
