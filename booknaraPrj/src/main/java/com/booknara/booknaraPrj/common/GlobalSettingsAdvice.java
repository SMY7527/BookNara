package com.booknara.booknaraPrj.common;

import com.booknara.booknaraPrj.admin.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSettingsAdvice {

    private final SettingsService settingsService;

    @ModelAttribute("adminEmail")
    public String adminEmail() {
        return settingsService.getAdminEmail();
    }

    @ModelAttribute("adminPhone")
    public String adminPhone() {
        return settingsService.getAdminPhone();
    }
}
