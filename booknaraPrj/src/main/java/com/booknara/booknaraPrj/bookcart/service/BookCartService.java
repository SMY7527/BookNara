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

@Service
@RequiredArgsConstructor
public class BookCartService {

    private final BookCartMapper mapper;
    private final BookCommandService circulationCommandService;
    public void add(String userId, String isbn13) {
        mapper.insert(userId, isbn13);
    }

    public void remove(String userId, Long cartId) {
        mapper.delete(userId, cartId);
    }

    public void clear(String userId) {
        mapper.deleteAll(userId);
    }

    public boolean toggle(String userId, String isbn13) {
        // 1) 있으면 삭제 시도
        int deleted = mapper.deleteByIsbn(userId, isbn13);
        if (deleted > 0) return false; // 이제 장바구니 아님

        // 2) 없으면 추가
        mapper.insert(userId, isbn13);
        return true; // 이제 장바구니 됨
    }

    public List<BookCartDTO> listWithLendable(String userId) {
        List<BookCartDTO> list = mapper.selectList(userId);
        for (BookCartDTO dto : list) {
            dto.setLendableYn(isLendable(dto.getIsbn13()));
        }
        return list;
    }

    public LendQuotaDTO getLendQuota(String userId) {
        LendQuotaDTO dto = new LendQuotaDTO();

        Integer maxObj = mapper.selectMaxLendCount();
        int max = (maxObj == null ? 5 : maxObj);
        if (max <= 0) max = 5;

        int current = mapper.countMyActiveLends(userId);
        int cart = mapper.countMyCart(userId);


        int available = Math.max(max - current, 0); // 남은 대여 가능(장바구니 반영 전)

        dto.setMaxLendCount(max);
        dto.setCurrentLendCount(current);
        dto.setCartCount(cart);
        dto.setAvailableCount(available);

        return dto;
    }

    public boolean isInCart(String userId, String isbn13) {
        return mapper.existsByIsbn(userId, isbn13) > 0;
    }

    public UserAddressDTO getMyDefaultAddress(String userId) {
        return mapper.selectMyDefaultAddress(userId);
    }

    @Transactional
    public void saveMyDefaultAddress(UserAddressDTO dto) {
        mapper.updateMyDefaultAddress(dto);
    }

    //대여 가능여부 판단
    public boolean isLendable(String isbn13) {
        return Boolean.TRUE.equals(mapper.isLendableByIsbn(isbn13));
    }

    @Transactional
    public void checkoutPaid(String userId) {
        // 1) 장바구니 재조회
        var items = listWithLendable(userId);
        if (items == null || items.isEmpty()) throw new IllegalStateException("장바구니가 비어있습니다.");

        // 2) lendable 검사
        boolean hasUnlendable = items.stream().anyMatch(it -> it.getLendableYn() == null || !it.getLendableYn());
        if (hasUnlendable) throw new IllegalStateException("대여 불가 도서가 포함되어 있습니다.");

        // 3) quota 검사
        var quota = getLendQuota(userId);
        if (quota != null && quota.getAvailableCount() < quota.getCartCount()) {
            throw new IllegalStateException("대여 가능 권수를 초과했습니다.");
        }

        // 4) 종이책만 lend (전자책은 정책대로 처리)
        for (var it : items) {
            boolean isEbook = "Y".equalsIgnoreCase(it.getEbookYn());
            if (!isEbook) {
                circulationCommandService.lend(it.getIsbn13(), userId);
            } else {
            }
        }

        // 5) 성공 시 카트 비우기
        clear(userId);
    }


}
