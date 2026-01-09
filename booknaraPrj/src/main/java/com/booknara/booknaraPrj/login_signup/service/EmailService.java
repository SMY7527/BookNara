package com.booknara.booknaraPrj.login_signup.service;




public interface EmailService {
    void sendVerifyCode(String to, String code);
}
