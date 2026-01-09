package com.booknara.booknaraPrj.admin.settings;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private final SettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public Settings getSettings() {
        // DB에 설정이 없으면 기본 객체를 반환하거나 에러를 방지함
        return settingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new Settings());
    }

    @Transactional
    public void updateSettings(Settings newSettings) {
        Settings existing = settingsRepository.findFirstByOrderBySettingsIdAsc()
                .orElse(new Settings());

        // 기존 데이터에 덮어쓰기
        existing.setDefaultLendDays(newSettings.getDefaultLendDays());
        existing.setDefaultExtendDays(newSettings.getDefaultExtendDays());
        existing.setLateFeePerDay(newSettings.getLateFeePerDay());
        existing.setLatePenaltyDays(newSettings.getLatePenaltyDays());
        existing.setMaxLendCount(newSettings.getMaxLendCount());
        existing.setDbUpdateCycleDays(newSettings.getDbUpdateCycleDays());
        existing.setAdminEmail(newSettings.getAdminEmail());
        existing.setAdminPhone(newSettings.getAdminPhone());

        settingsRepository.save(existing);
    }
}
