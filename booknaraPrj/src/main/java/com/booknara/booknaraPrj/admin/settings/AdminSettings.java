package com.booknara.booknaraPrj.admin.settings;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "SETTINGS")
public class AdminSettings {
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