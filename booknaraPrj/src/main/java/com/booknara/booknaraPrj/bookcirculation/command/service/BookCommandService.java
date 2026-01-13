package com.booknara.booknaraPrj.bookcirculation.command.service;

import com.booknara.booknaraPrj.bookcart.service.BookCartService;
import com.booknara.booknaraPrj.bookcirculation.command.dto.*;
import com.booknara.booknaraPrj.bookcirculation.command.mapper.BookCommandMapper;
import com.booknara.booknaraPrj.bookcirculation.status.controller.BookCirculationController;
import com.booknara.booknaraPrj.bookcirculation.status.mapper.BookCirculationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookCommandService {

    private final BookCommandMapper mapper;

    private void requireLogin(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
    }

    private void requireNotBlockedOrOverdue(String userId) {
        String blockedYn = mapper.selectUserBlockedYn(userId);
        String overdueYn = mapper.selectUserOverdueYn(userId);
        if ("Y".equals(blockedYn)) throw new IllegalStateException("차단된 사용자입니다.");
        if ("Y".equals(overdueYn)) throw new IllegalStateException("연체 상태에서는 이용할 수 없습니다.");
    }

    private String newLendId() {
        // 포맷  LEND_YYYYMMDD
        return "LEND_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String newRsvId() {
        return "RSV_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + UUID.randomUUID().toString().substring(0, 6);
    }



    @Transactional
    public LendResultDTO lend(String isbn13, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        Integer maxObj = mapper.selectMaxLendCount();
        int max = (maxObj == null ? 5 : maxObj);
        if (max <= 0) max = 5;

        int cur = mapper.countMyActiveLends(userId);
        if (cur >= max) {
            throw new IllegalStateException("대여 가능 권수를 초과했습니다. (최대 " + max + "권)");
        }



        // 1) 유저 동일 ISBN 활성 대여 여부 (정책)
        if (mapper.existsActiveLendByUserAndIsbn(userId, isbn13) == 1) {
            throw new IllegalStateException("이미 대여 중인 도서입니다.");
        }

        // 2) 대여 가능한 BOOK_ID 선점 (동시성 핵심)
        Long bookId = mapper.selectAvailableBookIdForUpdate(isbn13);
        if (bookId == null) {
            throw new IllegalStateException("대여 가능한 도서가 없습니다.");
        }

        // 3) LENDS 생성
        String lendId = newLendId();
        int inserted;
        try {
            inserted = mapper.insertLend(lendId, bookId, userId, isbn13);
        } catch (DuplicateKeyException e) {
            // 동시성에서 유니크(UQ_LENDS_BOOK_ACTIVE / UQ_LENDS_USER_ISBN_ACTIVE)로 튕길 수 있음
            throw new IllegalStateException("다른 사용자가 먼저 대여했습니다. 다시 시도하세요.");
        }
        if (inserted != 1) throw new IllegalStateException("대여 생성 실패");

        LendResultDTO out = new LendResultDTO();
        out.setLendId(lendId);
        out.setBookId(bookId);
        out.setIsbn13(isbn13);
        return out;
    }

    @Transactional
    public ExtendResultDTO extend(String lendId, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        // 연장 가능 조건은 너의 status 쿼리 기준으로:
        // EXTEND_CNT=0 AND RETURN_DUE_DATE <= NOW()+7days
        int ok = mapper.extendIfAllowed(lendId, userId);
        ExtendResultDTO out = new ExtendResultDTO();
        out.setLendId(lendId);
        out.setExtendedYn(ok == 1 ? "Y" : "N");
        out.setMessage(ok == 1 ? "연장 완료" : "연장 불가 조건입니다.");
        return out;
    }

    @Transactional
    public ReserveResultDTO reserve(String isbn13, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        // 1) 예약 제한(10명)
        int activeCnt = mapper.countActiveReservations(isbn13); // ACTIVE_FLAG=1 기준
        if (activeCnt >= 10) throw new IllegalStateException("예약 인원이 가득 찼습니다.");

        // 2) 예약 생성 (DB 유니크로 동일유저/동일ISBN ACTIVE/HOLD 중복 방지)
        String rsvId = newRsvId();
        try {
            int inserted = mapper.insertReservation(rsvId, userId, isbn13);
            if (inserted != 1) throw new IllegalStateException("예약 생성 실패");
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("이미 예약한 도서입니다.");
        }

        ReserveResultDTO out = new ReserveResultDTO();
        out.setRsvId(rsvId);
        out.setIsbn13(isbn13);
        return out;
    }

    @Transactional
    public CancelReserveResultDTO cancelReserve(String rsvId, String userId) {
        requireLogin(userId);

        int updated = mapper.cancelReservation(rsvId, userId);
        CancelReserveResultDTO out = new CancelReserveResultDTO();
        out.setRsvId(rsvId);
        out.setCancelledYn(updated == 1 ? "Y" : "N");
        return out;
    }

    @Transactional
    public ReturnBoxResultDTO returnToBox(String lendId, Long boxId, String userId) {
        requireLogin(userId);

        int updated = mapper.markReturnBox(lendId, userId, boxId);
        ReturnBoxResultDTO out = new ReturnBoxResultDTO();
        out.setLendId(lendId);
        out.setBoxedYn(updated == 1 ? "Y" : "N");
        return out;
    }

    @Transactional
    public ReturnConfirmResultDTO confirmReturn(String lendId, String userId) {
        requireLogin(userId);

        int updated = mapper.confirmReturn(lendId, userId);
        ReturnConfirmResultDTO out = new ReturnConfirmResultDTO();
        out.setLendId(lendId);
        out.setReturnedYn(updated == 1 ? "Y" : "N");
        return out;
    }
}
