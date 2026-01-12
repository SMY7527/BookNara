package com.booknara.booknaraPrj.mypage.withdraw;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final WithdrawMapper withdrawMapper;

    // ✅ 프로젝트에 이미 PasswordEncoder가 있으면 그걸 주입해서 써도 됨
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String CAPTCHA_KEY = "WITHDRAW_CAPTCHA";
    private static final SecureRandom RND = new SecureRandom();

    public byte[] generateCaptchaPng(HttpSession session) {
        String code = randomCode(5);
        session.setAttribute(CAPTCHA_KEY, code);

        BufferedImage img = new BufferedImage(180, 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 180, 48);

        // 노이즈 선
        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 8; i++) {
            int x1 = RND.nextInt(180), y1 = RND.nextInt(48);
            int x2 = RND.nextInt(180), y2 = RND.nextInt(48);
            g.drawLine(x1, y1, x2, y2);
        }

        // 문자
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.BLACK);
        g.drawString(code, 20, 34);

        g.dispose();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public boolean withdraw(String userId, String rawPassword, String captchaInput, HttpSession session) {
        String captcha = (String) session.getAttribute(CAPTCHA_KEY);
        if (captcha == null || !captcha.equalsIgnoreCase(captchaInput)) return false;

        String dbPw = withdrawMapper.selectPassword(userId);
        if (dbPw == null) return false;

        // dbPw가 암호화(BCrypt)라고 가정
        boolean pwOk = encoder.matches(rawPassword, dbPw);
        if (!pwOk) return false;

        // ✅ 삭제 X, 상태만 변경
        withdrawMapper.updateUserState(userId, "WITHDRAWN");
        return true;
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(RND.nextInt(chars.length())));
        return sb.toString();
    }
}

