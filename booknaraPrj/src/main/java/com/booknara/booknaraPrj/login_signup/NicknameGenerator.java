package com.booknara.booknaraPrj.login_signup;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {

    private static final List<String> SPORTS_VERBS = List.of(
            "물먹는",
            "늦잠 잔",
            "자유를 찾는",
            "행복한",
            "과자먹는",
            "토론하는",
            "등산하는",
            "슬픈",
            "비몽사몽한",
            "집중하는",
            "눕고싶은",
            "꿈을 꾸는",
            "이상한 나라의",
            "옆집 사는",
            "침대 밖이 싫은",
            "책읽기를 좋아하는",
            "데굴데굴 굴러가는",
            "침언젠간 우주를 정복할",
            "누가봐도 귀여운",
            "노래 하는",
            "활기찬",
            "고독한",
            "무적의",
            "잔잔한"


    );

    private static final List<String> ANIMALS = List.of(
            "초콜릿",
            "하프물범",
            "솜사탕",
            "말랑이",
            "여우",
            "토끼",
            "곰",
            "수세미",
            "스컹크",
            "몽구스",
            "미어캣",
            "하이에나",
            "돌고래",
            "펭귄",
            "고릴라",
            "판다",
            "하마",
            "얼룩말",
            "코알라",
            "나무늘보",
            "카피바라",
            "책갈피",
            "참기름",
            "두더지",
            "만두",
            "젤리",
            "두부"




    );

    private static final Random random = new Random();

    public static String generate() {
        String verb = SPORTS_VERBS.get(random.nextInt(SPORTS_VERBS.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));

        // 중복 확률 낮추기용 숫자
        int suffix = random.nextInt(1000);

        return verb +" "+ animal + suffix;
    }
}

