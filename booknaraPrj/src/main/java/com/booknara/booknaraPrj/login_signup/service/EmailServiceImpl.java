package com.booknara.booknaraPrj.login_signup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerifyCode(String to, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[북나라] 비밀번호 재설정 인증코드");
        message.setText(
                "안녕하세요.\n\n" +
                        "비밀번호 재설정을 위한 인증코드입니다.\n\n" +
                        "인증코드: " + code + "\n\n" +
                        "5분 이내에 입력해주세요.\n\n" +
                        "감사합니다.\n북나라 드림"
        );

        mailSender.send(message);
    }
}

