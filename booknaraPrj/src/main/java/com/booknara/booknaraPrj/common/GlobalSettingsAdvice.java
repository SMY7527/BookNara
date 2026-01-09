package com.booknara.booknaraPrj.common;

import com.booknara.booknaraPrj.admin.settings.AdminSettings;
import com.booknara.booknaraPrj.admin.settings.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSettingsAdvice {

    private final AdminSettings adminSettings;

    @ModelAttribute("adminEmail")
    public String adminEmail() {
        return adminSettings.getAdminEmail();
    }

    @ModelAttribute("adminPhone")
    public String adminPhone() {
        return adminSettings.getAdminPhone();
    }
}
