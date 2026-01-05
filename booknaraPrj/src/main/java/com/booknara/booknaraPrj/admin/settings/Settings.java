package com.booknara.booknaraPrj.admin.settings;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SETTINGS")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingsId;

    private int defaultLendDays;
    private int defaultExtendDays;
    private int lateFeePerDay;
    private int latePenaltyDays;
    private int maxLendCount;
    private int dbUpdateCycleDays;
    private String adminEmail;
    private String adminPhone;
}