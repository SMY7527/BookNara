package com.booknara.booknaraPrj.bookcart.controller;

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
        return "bookcart/cart";
    }

    @PostMapping("/{isbn13}/toggle")
    @ResponseBody
    public java.util.Map<String, Object> toggle(@PathVariable String isbn13, Authentication auth) {
        boolean inCart = service.toggle(getUserId(auth), isbn13);
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
}
