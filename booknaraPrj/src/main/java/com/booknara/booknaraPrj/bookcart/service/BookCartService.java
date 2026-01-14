package com.booknara.booknaraPrj.bookcart.service;

import com.booknara.booknaraPrj.bookcart.dto.BookCartDTO;
import com.booknara.booknaraPrj.bookcart.dto.LendQuotaDTO;
import com.booknara.booknaraPrj.bookcart.dto.UserAddressDTO;
import com.booknara.booknaraPrj.bookcart.mapper.BookCartMapper;
import com.booknara.booknaraPrj.bookcirculation.command.service.BookCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [BookCartService]
 * 사용자의 장바구니(대여 바구니) 관리와 최종 대여 프로세스(Checkout)를 총괄하는 서비스입니다.
 * 도서관의 대여 정책(한도)을 준수하며, 실물 도서와 전자책의 대여 경로를 분기 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용
public class BookCartService {

    private final BookCartMapper mapper;
    private final BookCommandService circulationCommandService;

    /** 장바구니 추가 */
    @Transactional
    public void add(String userId, String isbn13) {
        mapper.insert(userId, isbn13);
    }

    /** 장바구니 개별 항목 삭제 */
    @Transactional
    public void remove(String userId, Long cartId) {
        mapper.delete(userId, cartId);
    }

    /** 장바구니 전체 비우기 */
    @Transactional
    public void clear(String userId) {
        mapper.deleteAll(userId);
    }

    /**
     * [장바구니 토글 기능]
     * 이미 담긴 책이면 제거하고, 없으면 추가합니다. 도서 상세/목록 UI의 버튼 하나로 두 기능을 제어할 때 사용됩니다.
     */
    @Transactional
    public boolean toggle(String userId, String isbn13) {
        // 1) 존재 확인 후 삭제 시도
        int deleted = mapper.deleteByIsbn(userId, isbn13);
        if (deleted > 0) return false; // 삭제되었으므로 현재 장바구니 상태 아님

        // 2) 없었으므로 새로 추가
        mapper.insert(userId, isbn13);
        return true; // 추가되었으므로 현재 장바구니 상태임
    }

    /**
     * [실시간 재고 반영 목록 조회]
     * 장바구니 리스트를 불러올 때, 담긴 시점과 현재 시점 사이의 재고 변화를 실시간으로 반영하여
     * '대여 가능 여부(lendableYn)'를 세팅합니다.
     */
    public List<BookCartDTO> listWithLendable(String userId) {
        List<BookCartDTO> list = mapper.selectList(userId);
        for (BookCartDTO dto : list) {
            // 각 도서별 실시간 재고 상태 확인 (N+1 이슈 가능성이 있으나 정확한 상태 전달을 우선함)
            dto.setLendableYn(isLendable(dto.getIsbn13()));
        }
        return list;
    }

    /**
     * [대여 쿼터(한도) 계산]
     * 도서관 정책(최대 5권 등) 대비 사용자가 현재 몇 권을 더 빌릴 수 있는지 계산합니다.
     */
    public LendQuotaDTO getLendQuota(String userId) {
        LendQuotaDTO dto = new LendQuotaDTO();

        // 1. 시스템 설정에서 인당 최대 대여 한도 조회 (기본값 5)
        Integer maxObj = mapper.selectMaxLendCount();
        int max = (maxObj == null ? 5 : maxObj);
        if (max <= 0) max = 5;

        // 2. 현재 대여 중인 도서 수와 장바구니에 담긴 수 집계
        int current = mapper.countMyActiveLends(userId);
        int cart = mapper.countMyCart(userId);

        // 3. 잔여 대여 가능 권수 계산 (이미 빌린 책 제외)
        int available = Math.max(max - current, 0);

        dto.setMaxLendCount(max);
        dto.setCurrentLendCount(current);
        dto.setCartCount(cart);
        dto.setAvailableCount(available);

        return dto;
    }

    /** 장바구니 담김 여부 확인 */
    public boolean isInCart(String userId, String isbn13) {
        return mapper.existsByIsbn(userId, isbn13) > 0;
    }

    /** 유저의 기본 배송/연락 주소 조회 */
    public UserAddressDTO getMyDefaultAddress(String userId) {
        return mapper.selectMyDefaultAddress(userId);
    }

    /** 유저의 기본 주소 정보 업데이트 */
    @Transactional
    public void saveMyDefaultAddress(UserAddressDTO dto) {
        mapper.updateMyDefaultAddress(dto);
    }

    /** 특정 ISBN 도서의 현재 대여 가능 상태 여부 반환 */
    public boolean isLendable(String isbn13) {
        return Boolean.TRUE.equals(mapper.isLendableByIsbn(isbn13));
    }

    /**
     * [최종 대여 실행 (Checkout)]
     * 장바구니에 담긴 도서들을 일괄 검증 후 대여 처리하고, 실물 도서의 경우 배송 정보를 생성합니다.
     * @throws IllegalStateException 검증 실패 시(재고 부족, 한도 초과 등) 예외 발생
     */
    @Transactional
    public void checkoutPaid(String userId) {
        // 1) 검증: 장바구니가 비어있는지 확인
        var items = listWithLendable(userId);
        if (items == null || items.isEmpty()) throw new IllegalStateException("장바구니가 비어있습니다.");

        // 2) 검증: 실시간 대여 가능 여부 (결제 버튼 누르는 찰나에 재고가 소진된 경우 방어)
        boolean hasUnlendable = items.stream().anyMatch(it -> it.getLendableYn() == null || !it.getLendableYn());
        if (hasUnlendable) throw new IllegalStateException("대여 불가 도서가 포함되어 있습니다. 목록을 확인해주세요.");

        // 3) 검증: 대여 한도 정책 준수 여부
        var quota = getLendQuota(userId);
        if (quota != null && quota.getAvailableCount() < quota.getCartCount()) {
            throw new IllegalStateException("대여 가능 권수를 초과했습니다. 현재 대여 한도를 확인해주세요.");
        }

        // 4) 실행: 도서별 대여 처리 루프

        for (var it : items) {
            // 대여 서비스 호출을 통해 실제 대여 이력(LEND) 생성
            com.booknara.booknaraPrj.bookcirculation.command.dto.LendResultDTO result
                    = circulationCommandService.lend(it.getIsbn13(), userId);

            String lendId = result.getLendId();

            // 5) 종이책 특화 로직: 종이책이고 대여가 성공했다면 '배송(Delivery)' 테이블에 초기 데이터 생성
            if (!"Y".equalsIgnoreCase(it.getEbookYn()) && lendId != null) {
                mapper.insertDelivery(lendId);
            }
        }

        // 6) 완료: 대여가 성공적으로 마무리되면 장바구니를 비움
        clear(userId);
    }
}