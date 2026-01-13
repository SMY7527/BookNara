package com.booknara.booknaraPrj.mypage.info;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import com.booknara.booknaraPrj.login_signup.service.UserPreferGenreService;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/myinfo")
public class MyInfoController {

    private final MyInfoService myInfoService;
    private final UserPreferGenreService userPreferGenreService;
    private final UserMapper userMapper;

    @GetMapping
    public String myInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        String userId = userDetails.getUserId();

        MyInfoDto myInfo = myInfoService.getMyInfo(userId);

        if (myInfo == null) { // DB에 없을 때 대비
            myInfo = new MyInfoDto();
            myInfo.setUserId(userId);
        }

        model.addAttribute("myInfo", myInfo);
        model.addAttribute("loginUser", userDetails);
        model.addAttribute("content", "mypage/myinfo");
        model.addAttribute(
                "userGenres",
                userPreferGenreService.getActiveGenres(userId)
        );
        return "mypage/mypagelayout";
    }



    @PostMapping("/update")
    public String updateMyInfo(MyInfoDto dto,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUserId();
        dto.setUserId(userId);

        // 1️⃣ DB 업데이트
        myInfoService.updateMyInfo(dto);

        // 2️⃣ DB에서 User를 다시 조회 (중요)
        User refreshedUser = userMapper.findByUserId(userId);

        // 3️⃣ 새 principal 생성
        CustomUserDetails newPrincipal = new CustomUserDetails(refreshedUser);

        // 4️⃣ SecurityContext 교체
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth =
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        currentAuth.getCredentials(),
                        newPrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/mypage/myinfo";
    }

    @PostMapping("/genres")
    @ResponseBody
    public void updateGenres(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestBody List<Integer> genreIds) {

        if (userDetails == null) {
            throw new RuntimeException("로그인 필요");
        }

        userPreferGenreService.update(userDetails.getUserId(), genreIds);
    }



}
