package com.booknara.booknaraPrj.bookcirculation.dto;

import lombok.Data;

@Data
public class BookCirculationStatusDTO {
    private String isbn13;

    private int ownedCnt;       // 소장(정상)
    private int lendingCnt;     // 대여중
    private int availableCnt;   // 대여가능 = owned - lending
    private int rsvActiveCnt;   // 예약중(Active)
    private String rsvLimitYn;  // 'Y' if >=10 else 'N'

    private String myLendYn;    // 'Y'/'N'
    private String myExtendYn;  // 'Y'/'N' (연장 가능 조건)
    private String myLendId;

    private String myRsvYn;     // 'Y'/'N'
    private String myRsvId;

    private String userBlockedYn; // 'Y'/'N'
    private String userOverdueYn; // 'Y'/'N'
}
