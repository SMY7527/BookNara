package com.booknara.booknaraPrj.mypage.info;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final MyInfoMapper myInfoMapper;

    public MyInfoDto getMyInfo(String userId) {
        return myInfoMapper.selectMyInfo(userId);
    }

    public void updateMyInfo(MyInfoDto dto) {
        myInfoMapper.updateMyInfo(dto);
    }
}

