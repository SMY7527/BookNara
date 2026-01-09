package com.booknara.booknaraPrj.common;

import com.booknara.booknaraPrj.admin.settings.AdminSettings;
import com.booknara.booknaraPrj.admin.settings.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSettingsAdvice {

    private final AdminSettingsService adminSettingsService;

    @ModelAttribute("adminSettings")
    public AdminSettings globalSettings() {
        // 서비스의 메서드를 호출하여 실제 DB에 저장된 설정 객체를 가져옵니다.
        // 메서드 이름은 AdminSettingsService에 정의된 것을 확인하세요 (예: getSettings)
        return adminSettingsService.getSettings();
    }

    @ModelAttribute("adminEmail")
    public String adminEmail() {
        AdminSettings settings = adminSettingsService.getSettings();
        return settings.getAdminEmail();
    }

    @ModelAttribute("adminPhone")
    public String adminPhone() {
        AdminSettings settings = adminSettingsService.getSettings();
        return settings.getAdminPhone();
    }
}
