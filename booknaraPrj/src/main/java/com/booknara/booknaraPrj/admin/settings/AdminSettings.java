package com.booknara.booknaraPrj.admin.settings;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "SETTINGS")
public class AdminSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SETTINGS_ID")
    private Long settingsId;

    @Column(name = "DEFAULT_LEND_DAYS")
    private int defaultLendDays;

    @Column(name = "DEFAULT_EXTEND_DAYS")
    private int defaultExtendDays;

    @Column(name = "LATE_FEE_PER_DAY")
    private int lateFeePerDay;

    @Column(name = "LATE_PENALTY_DAYS")
    private int latePenaltyDays;

    @Column(name = "MAX_LEND_COUNT")
    private int maxLendCount;

    @Column(name = "DB_UPDATE_CYCLE_DAYS")
    private int dbUpdateCycleDays;

    @Column(name = "ADMIN_EMAIL")
    private String adminEmail;

    @Column(name = "ADMIN_PHONE")
    private String adminPhone;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}