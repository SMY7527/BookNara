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

        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUserId();
        MyInfoDto myInfo = myInfoService.getMyInfo(userId);

        if (myInfo == null) {
            myInfo = new MyInfoDto();
            myInfo.setUserId(userId);
        }

        model.addAttribute("myInfo", myInfo);
        model.addAttribute("loginUser", userDetails);
        model.addAttribute("content", "mypage/myinfo");
        model.addAttribute("userGenres", userPreferGenreService.getActiveGenres(userId));

        return "mypage/mypagelayout";
    }

    @PostMapping("/update")
    public String updateMyInfo(MyInfoDto dto,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUserId();
        dto.setUserId(userId);

        // ✅ 1) 닉네임 중복 체크(나 제외)
        // dto.getProfileNm() 가 null/공백일 수도 있으니 방어
        String newProfileNm = dto.getProfileNm() == null ? "" : dto.getProfileNm().trim();

        if (!newProfileNm.isEmpty()) {
            boolean available = myInfoService.isProfileNameAvailableForUpdate(newProfileNm, userId);
            if (!available) {
                // ✅ 중복이면 업데이트 중단 + 같은 페이지 다시 렌더링 + 메시지 전달
                MyInfoDto myInfo = myInfoService.getMyInfo(userId);

                model.addAttribute("myInfo", myInfo);
                model.addAttribute("loginUser", userDetails);
                model.addAttribute("content", "mypage/myinfo");
                model.addAttribute("userGenres", userPreferGenreService.getActiveGenres(userId));

                // input 옆에 띄울 메시지
                model.addAttribute("profileNmMsg", "이미 사용 중인 닉네임입니다.");
                model.addAttribute("profileNmMsgType", "fail"); // ok / fail 같은 css용
                model.addAttribute("editMode", true); // (선택) 다시 수정모드로 유지용

                return "mypage/mypagelayout";
            }

            // 중복 아니면 trim된 값으로 다시 세팅(깔끔)
            dto.setProfileNm(newProfileNm);
        }

        // ✅ 2) DB 업데이트
        myInfoService.updateMyInfo(dto);

        // ✅ 3) principal 갱신(헤더 닉네임 등 즉시 반영)
        User refreshedUser = userMapper.findByUserId(userId);
        CustomUserDetails newPrincipal = new CustomUserDetails(refreshedUser);

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
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

        if (userDetails == null) throw new RuntimeException("로그인 필요");

        userPreferGenreService.update(userDetails.getUserId(), genreIds);
    }

    // ✅ /mypage/profile/check 매핑은 이제 필요 없으니 삭제(또는 주석처리)
}
