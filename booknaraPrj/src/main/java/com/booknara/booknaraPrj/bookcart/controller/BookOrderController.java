package com.booknara.booknaraPrj.bookcart.controller;

import com.booknara.booknaraPrj.bookcart.service.BookCartService;
import com.booknara.booknaraPrj.bookcart.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/book/order")
public class BookOrderController {

    private final BookCartService cartService;
    private final OrderPaymentService orderPaymentService;

    @GetMapping
    public String orderPage(Authentication auth, Model model){
        String userId = auth.getName();

        var items = cartService.list(userId);

        boolean hasPaper = items.stream().anyMatch(it -> !"Y".equalsIgnoreCase(it.getEbookYn()));
        int expectedPrice = hasPaper ? 3000 : 0;

        model.addAttribute("items", items);
        model.addAttribute("hasPaper", hasPaper);
        model.addAttribute("expectedPrice", expectedPrice);

        // step 표시용
        model.addAttribute("step", "PAY");

        return "bookcart/order"; // templates/bookcart/order.html
    }

    /** 전자책-only 무료 확정 */
    @PostMapping("/confirm-free")
    @ResponseBody
    public Map<String,Object> confirmFree(Authentication auth){
        String userId = auth.getName();
        orderPaymentService.confirmFreeOrder(userId); // LENDS 생성 + CART clear
        return Map.of("ok", true);
    }
}

