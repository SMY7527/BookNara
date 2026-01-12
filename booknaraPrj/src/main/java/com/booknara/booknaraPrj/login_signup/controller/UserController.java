package com.booknara.booknaraPrj.login_signup.controller;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.dto.ExtraAddressRequest;
import com.booknara.booknaraPrj.login_signup.dto.PreferGenreRequest;
import com.booknara.booknaraPrj.login_signup.dto.SignupRequest;
import com.booknara.booknaraPrj.login_signup.service.UserPreferGenreService;
import com.booknara.booknaraPrj.login_signup.service.UserService1;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import com.booknara.booknaraPrj.security.oauth.CustomOAuth2User;
import com.booknara.booknaraPrj.security.oauth.SocialLinkSessionKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService1 userService1;
    private final AuthenticationManager authenticationManager;
    private final UserPreferGenreService userPreferGenreService;

    public UserController(UserService1 userService1,
                          AuthenticationManager authenticationManager,
                          UserPreferGenreService userPreferGenreService) {
        this.userService1 = userService1;
        this.authenticationManager = authenticationManager;
        this.userPreferGenreService = userPreferGenreService;
    }
    //로그인

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


    //회원가입
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest req,
                         RedirectAttributes ra,
                         HttpServletRequest request) {

        try {
            userService1.signup(req);

            // ⭐ 자동 로그인 (raw 비번으로 인증)
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(req.getUserId(), req.getPassword());

            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 세션에도 SecurityContext 저장(안전하게)
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            return "redirect:/users/signup-extra";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/signup";
        }
    }

    //소셜로그인 연동x
    @PostMapping("/signup-social")
    public String signupSocial(HttpServletRequest request,
                               HttpSession session,
                               RedirectAttributes ra) {

        try {
            String userId = userService1.signupSocial(session);

            // 로그인
            forceLogin(userId, request);

            // 세션 정리
            clearSocialSession(session);

            return "redirect:/users/signup-extra";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/login";
        }


    }

    private void forceLogin(String userId, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        null
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    private void clearSocialSession(HttpSession session) {
        session.removeAttribute(SocialLinkSessionKey.LINK_PROVIDER);
        session.removeAttribute(SocialLinkSessionKey.LINK_PROVIDER_ID);
        session.removeAttribute(SocialLinkSessionKey.LINK_EMAIL);
        session.removeAttribute(SocialLinkSessionKey.LINK_USER_ID);
    }





    //주소,장르선택
    @GetMapping("/signup-extra")
    public String signupExtraPage() {
        return "signup-extra"; // templates/signup-extra.html
    }

    @PostMapping("/extra-complete")
    @ResponseBody
    public void completeExtra(Authentication auth) {

        Object principal = auth.getPrincipal();
        User user;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            user = oAuth2User.getUser();
        } else if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else {
            throw new IllegalStateException("알 수 없는 로그인 타입");
        }

        // 1️⃣ DB 업데이트
        userService1.updateExtraInfoDone(user.getUserId());

        // 2️⃣ ⭐ 세션 principal 안의 User도 같이 갱신
        user.setExtraInfoDone("Y");
    }

    //주소저장
    @PostMapping("/extra-address")
    @ResponseBody
    public void saveExtraAddress(@RequestBody ExtraAddressRequest req,
                                 Authentication auth) {

        Object principal = auth.getPrincipal();
        User user;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            user = oAuth2User.getUser();
        } else if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else {
            throw new IllegalStateException("알 수 없는 로그인 타입");
        }

        //  주소 저장
        userService1.updateAddress(
                user.getUserId(),
                req.getZipcode(),
                req.getAddr(),
                req.getDetailAddr()
        );

        // 추가정보 완료 처리
        userService1.updateExtraInfoDone(user.getUserId());

        //  세션 객체 갱신
        user.setExtraInfoDone("Y");
        user.setZipcode(req.getZipcode());
        user.setAddr(req.getAddr());
        user.setDetailAddr(req.getDetailAddr());
    }

    //선호장르 저장




    @PostMapping("/prefer-genres")
    @ResponseBody
    public ResponseEntity<Void> savePreferGenres(
            @RequestBody PreferGenreRequest request,
            Authentication auth
    ) {
        Object principal = auth.getPrincipal();
        User user;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            user = oAuth2User.getUser();
        } else if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Integer> genreIds = request.getGenreIds();
        if (genreIds == null || genreIds.isEmpty() || genreIds.size() > 3) {
            return ResponseEntity.badRequest().build();
        }

        userPreferGenreService.save(user.getUserId(), genreIds);

        userService1.updateExtraInfoDone(user.getUserId());
        user.setExtraInfoDone("Y");

        return ResponseEntity.ok().build();
        }
    }


