package com.booknara.booknaraPrj.bookcart.controller;

import com.booknara.booknaraPrj.bookcart.dto.UserAddressDTO;
import com.booknara.booknaraPrj.bookcart.service.BookCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * [BookCartController]
 * 장바구니 관리, 주소 설정, 그리고 최종 대여 결제(Checkout)를 담당하는 컨트롤러입니다.
 * 모든 대여 프로세스의 진입점 역할을 수행합니다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/book/cart")
public class BookCartController {

    private final BookCartService service;

    /**
     * [공통 로직: 사용자 ID 추출]
     * Spring Security의 Authentication 객체에서 현재 로그인한 사용자 ID를 가져옵니다.
     * 비로그인(익명) 사용자인 경우 401 Unauthorized 예외를 발생시켜 접근을 차단합니다.
     */
    private String getUserId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다.");
        }
        return auth.getName();
    }

    /**
     * [장바구니 메인 페이지]
     * 사용자의 장바구니 목록, 대여 한도(Quota), 배송 필요 여부(Paper book 존재 여부)를 모델에 담아 반환합니다.
     */
    @GetMapping
    public String cartPage(Authentication auth, Model model) {
        String userId = getUserId(auth);
        var items = service.listWithLendable(userId);

        // 장바구니 내 도서 중 종이책이 하나라도 있는지 확인 (배송 주소 UI 노출 여부 결정)
        boolean hasPaper = items.stream().anyMatch(it -> !"Y".equalsIgnoreCase(it.getEbookYn()));

        model.addAttribute("items", items);
        model.addAttribute("quota", service.getLendQuota(userId));
        model.addAttribute("hasPaper", hasPaper);

        return "bookcart/cart";
    }

    /**
     * [장바구니 토글 API]
     * 도서 상세/검색 목록에서 '담기/취소'를 비동기(AJAX)로 처리합니다.
     */
    @PostMapping("/{isbn13}/toggle")
    @ResponseBody
    public java.util.Map<String, Object> toggle(@PathVariable String isbn13, Authentication auth) {
        boolean inCart = service.toggle(getUserId(auth), isbn13);
        return java.util.Map.of("inCart", inCart);
    }

    /**
     * [장바구니 상태 조회 API]
     * 특정 도서가 현재 사용자의 장바구니에 담겨 있는지 확인합니다. (비로그인 시 false 반환)
     */
    @GetMapping("/{isbn13}/status")
    @ResponseBody
    public Map<String, Object> cartStatus(@PathVariable String isbn13, Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return Map.of("inCart", false);
        }
        String userId = auth.getName();
        boolean inCart = service.isInCart(userId, isbn13);
        return Map.of("inCart", inCart);
    }

    /** 장바구니 단건 추가 */
    @PostMapping("/add")
    @ResponseBody
    public void add(@RequestParam String isbn13, Authentication auth) {
        service.add(getUserId(auth), isbn13);
    }

    /** 장바구니 단건 제거 */
    @PostMapping("/remove")
    @ResponseBody
    public void remove(@RequestParam Long cartId, Authentication auth) {
        service.remove(getUserId(auth), cartId);
    }

    /** 장바구니 비우기 */
    @PostMapping("/clear")
    @ResponseBody
    public void clear(Authentication auth) {
        service.clear(getUserId(auth));
    }

    /**
     * [배송지 정보 조회 API]
     * 사용자의 기본 주소가 설정되어 있는지 확인하고 데이터를 반환합니다.
     */
    @GetMapping("/address/default")
    @ResponseBody
    public java.util.Map<String, Object> getDefaultAddress(Authentication auth) {
        String userId = getUserId(auth);
        UserAddressDTO dto = service.getMyDefaultAddress(userId);

        // 필수 주소 정보가 모두 채워져 있는지 검증
        boolean exists = dto != null
                && dto.getZipcode() != null && !dto.getZipcode().isBlank()
                && dto.getAddr() != null && !dto.getAddr().isBlank()
                && dto.getDetailAddr() != null && !dto.getDetailAddr().isBlank();

        return java.util.Map.of("exists", exists, "data", dto);
    }

    /** [배송지 정보 저장 API] 사용자가 새로 입력한 주소를 기본 주소로 저장합니다. */
    @PostMapping("/address/save-default")
    @ResponseBody
    public java.util.Map<String, Object> saveDefaultAddress(@RequestParam String zipcode,
                                                            @RequestParam String addr,
                                                            @RequestParam String detailAddr,
                                                            Authentication auth) {
        String userId = getUserId(auth);

        UserAddressDTO dto = new UserAddressDTO();
        dto.setUserId(userId);
        dto.setZipcode(zipcode);
        dto.setAddr(addr);
        dto.setDetailAddr(detailAddr);

        service.saveMyDefaultAddress(dto);
        return java.util.Map.of("ok", true);
    }

    /**
     * [최종 대여 실행 API]
     * 실제 대여 처리를 수행합니다. 재고 부족이나 한도 초과 시 409 Conflict 에러를 반환합니다.
     */
    @PostMapping("/checkout/paid")
    @ResponseBody
    public Map<String, Object> checkoutPaid(Authentication auth) {
        String userId = getUserId(auth);
        try {
            service.checkoutPaid(userId);
            return Map.of("ok", true);
        } catch (IllegalStateException e) {
            // 비즈니스 로직 위반 시(한도 초과 등) 충돌 상태 코드 반환
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /** [결제 결과 페이지] 대여 성공 후 완료 화면을 렌더링합니다. */
    @GetMapping("/result")
    public String resultPage(Model model) {
        model.addAttribute("step", "DONE");
        return "bookcart/result";
    }
}