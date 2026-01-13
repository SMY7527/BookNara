package com.booknara.booknaraPrj.mypage.info;

import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final MyInfoMapper myInfoMapper;
    private final UserMapper userMapper;

    public MyInfoDto getMyInfo(String userId) {
        return myInfoMapper.selectMyInfo(userId);
    }

    public void updateMyInfo(MyInfoDto dto) {
        myInfoMapper.updateMyInfo(dto);
    }

    public boolean isProfileNameAvailableForUpdate(String profileNm, String userId) {
        return userMapper.countByProfileNmExceptMe(profileNm, userId) == 0;
    }





}

