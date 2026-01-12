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

@Controller
@RequiredArgsConstructor
@RequestMapping("/book/cart")
public class BookCartController {

    private final BookCartService service;

    private String getUserId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return auth.getName();
    }


    @GetMapping
    public String cartPage(Authentication auth, Model model) {
        String userId = getUserId(auth);
        model.addAttribute("items", service.list(userId));
        model.addAttribute("quota", service.getLendQuota(userId));
        return "bookcart/cart";
    }


    @PostMapping("/{isbn13}/toggle")
    @ResponseBody
    public java.util.Map<String, Object> toggle(@PathVariable String isbn13, Authentication auth) {
        boolean inCart = service.toggle(getUserId(auth), isbn13);
        return java.util.Map.of("inCart", inCart);
    }

    @GetMapping("/{isbn13}/status")
    @ResponseBody
    public java.util.Map<String, Object> status(@PathVariable String isbn13, Authentication auth) {
        boolean inCart = service.isInCart(getUserId(auth), isbn13);
        return java.util.Map.of("inCart", inCart);
    }



    @PostMapping("/add")
    @ResponseBody
    public void add(@RequestParam String isbn13, Authentication auth) {
        service.add(getUserId(auth), isbn13);
    }

    @PostMapping("/remove")
    @ResponseBody
    public void remove(@RequestParam Long cartId, Authentication auth) {
        service.remove(getUserId(auth), cartId);
    }

    @PostMapping("/clear")
    @ResponseBody
    public void clear(Authentication auth) {
        service.clear(getUserId(auth));
    }

    @GetMapping("/address/default")
    @ResponseBody
    public java.util.Map<String, Object> getDefaultAddress(Authentication auth) {
        String userId = getUserId(auth);

        UserAddressDTO dto = service.getMyDefaultAddress(userId);

        boolean exists = dto != null
                && dto.getZipcode() != null && !dto.getZipcode().isBlank()
                && dto.getAddr() != null && !dto.getAddr().isBlank()
                && dto.getDetailAddr() != null && !dto.getDetailAddr().isBlank();

        return java.util.Map.of(
                "exists", exists,
                "data", dto
        );
    }

    /** 기본주소 저장 */
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
}
