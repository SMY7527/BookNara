package com.booknara.booknaraPrj.bookcirculation.command.service;

import com.booknara.booknaraPrj.bookcart.service.BookCartService;
import com.booknara.booknaraPrj.bookcirculation.command.dto.*;
import com.booknara.booknaraPrj.bookcirculation.command.mapper.BookCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * [BookCommandService]
 * 대출, 연장, 예약, 반납 등 도서의 상태를 변경하는 모든 비즈니스 로직을 수행합니다.
 * 모든 메서드는 데이터 정합성을 위해 @Transactional 환경에서 실행됩니다.
 */
@Service
@RequiredArgsConstructor
public class BookCommandService {

    private final BookCommandMapper mapper;

    /** 공통: 로그인 여부 확인 */
    private void requireLogin(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
    }

    /** 공통: 사용자의 서비스 이용 가능 상태(차단/연체 여부) 확인 */
    private void requireNotBlockedOrOverdue(String userId) {
        String blockedYn = mapper.selectUserBlockedYn(userId);
        String overdueYn = mapper.selectUserOverdueYn(userId);
        if ("Y".equals(blockedYn)) throw new IllegalStateException("차단된 사용자입니다.");
        if ("Y".equals(overdueYn)) throw new IllegalStateException("연체 상태에서는 이용할 수 없습니다.");
    }

    /** 대출 고유 번호 생성 (LEND_YYYYMMDD_랜덤6자리) */
    private String newLendId() {
        return "LEND_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    /** 예약 고유 번호 생성 (RSV_YYYYMMDD_랜덤6자리) */
    private String newRsvId() {
        return "RSV_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * [도서 대출 실행]
     * 1. 권한 및 연체 체크
     * 2. 대출 가능 권수(최대 5권 등) 체크
     * 3. 중복 대출 여부 체크
     * 4. 실물 도서 선점(Row-level Lock) 및 대출 기록 생성
     */
    @Transactional
    public LendResultDTO lend(String isbn13, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        // [정책] 최대 대출 권수 확인 (설정값 조회)
        Integer maxObj = mapper.selectMaxLendCount();
        int max = (maxObj == null ? 5 : maxObj);
        if (max <= 0) max = 5;

        int cur = mapper.countMyActiveLends(userId);
        if (cur >= max) {
            throw new IllegalStateException("대여 가능 권수를 초과했습니다. (최대 " + max + "권)");
        }

        // [정책] 동일 도서 중복 대여 방지
        if (mapper.existsActiveLendByUserAndIsbn(userId, isbn13) == 1) {
            throw new IllegalStateException("이미 대여 중인 도서입니다.");
        }

        // [핵심: 동시성 제어] 대여 가능한 실물 도서 1권을 찾고 해당 행을 잠금(FOR UPDATE)
        Long bookId = mapper.selectAvailableBookIdForUpdate(isbn13);
        if (bookId == null) {
            throw new IllegalStateException("대여 가능한 도서가 없습니다.");
        }

        // [데이터 생성] 대출 기록 삽입
        String lendId = newLendId();
        int inserted;
        try {
            inserted = mapper.insertLend(lendId, bookId, userId, isbn13);
        } catch (DuplicateKeyException e) {
            // DB 유니크 제약 조건에 의해 찰나의 순간에 발생한 중복 대출 시도 방어
            throw new IllegalStateException("다른 사용자가 먼저 대여했습니다. 다시 시도하세요.");
        }
        if (inserted != 1) throw new IllegalStateException("대여 생성 실패");

        LendResultDTO out = new LendResultDTO();
        out.setLendId(lendId);
        out.setBookId(bookId);
        out.setIsbn13(isbn13);
        return out;
    }

    /**
     * [대출 연장]
     * - 정책(연장 횟수 0회 및 반납 7일 전) 충족 시 반납 예정일을 7일 뒤로 갱신
     */
    @Transactional
    public ExtendResultDTO extend(String lendId, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        // SQL의 WHERE 절에서 모든 연장 정책을 한 번에 검증하여 원자성 확보
        int ok = mapper.extendIfAllowed(lendId, userId);

        ExtendResultDTO out = new ExtendResultDTO();
        out.setLendId(lendId);
        out.setExtendedYn(ok == 1 ? "Y" : "N");
        out.setMessage(ok == 1 ? "연장 완료" : "연장 불가 조건입니다.");
        return out;
    }

    /**
     * [도서 예약]
     * - 대출 가능한 도서가 없을 때 사용자가 대기 순번을 등록
     * - 한 도서당 최대 10명 제한
     */
    @Transactional
    public ReserveResultDTO reserve(String isbn13, String userId) {
        requireLogin(userId);
        requireNotBlockedOrOverdue(userId);

        // [정책] 예약 정원 초과 여부 확인
        int activeCnt = mapper.countActiveReservations(isbn13);
        if (activeCnt >= 10) throw new IllegalStateException("예약 인원이 가득 찼습니다.");

        String rsvId = newRsvId();
        try {
            int inserted = mapper.insertReservation(rsvId, userId, isbn13);
            if (inserted != 1) throw new IllegalStateException("예약 생성 실패");
        } catch (DuplicateKeyException e) {
            // 동일 유저가 동일 도서에 중복 예약을 거는 것을 DB 제약조건으로 방어
            throw new IllegalStateException("이미 예약한 도서입니다.");
        }

        ReserveResultDTO out = new ReserveResultDTO();
        out.setRsvId(rsvId);
        out.setIsbn13(isbn13);
        return out;
    }

    /** [예약 취소] 사용자가 대기 중인 예약을 직접 철회 */
    @Transactional
    public CancelReserveResultDTO cancelReserve(String rsvId, String userId) {
        requireLogin(userId);

        int updated = mapper.cancelReservation(rsvId, userId);
        CancelReserveResultDTO out = new CancelReserveResultDTO();
        out.setRsvId(rsvId);
        out.setCancelledYn(updated == 1 ? "Y" : "N");
        return out;
    }

    /** [반납함 투입] 무인 반납함에 도서를 넣었음을 기록 (1차 단계) */
    @Transactional
    public ReturnBoxResultDTO returnToBox(String lendId, Long boxId, String userId) {
        requireLogin(userId);

        int updated = mapper.markReturnBox(lendId, userId, boxId);
        ReturnBoxResultDTO out = new ReturnBoxResultDTO();
        out.setLendId(lendId);
        out.setBoxedYn(updated == 1 ? "Y" : "N");
        return out;
    }

    /** [반납 확정] 사서의 최종 승인이나 시스템 자동 확인을 통한 대출 종료 (2차 단계) */
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