package com.booknara.booknaraPrj.mainpage.service;

import com.booknara.booknaraPrj.login_signup.mapper.UserPreferGenreMapper;
import com.booknara.booknaraPrj.mainpage.dto.HashtagDTO;
import com.booknara.booknaraPrj.mainpage.dto.MallangPickDTO;
import com.booknara.booknaraPrj.mainpage.mapper.MallangPickMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MallangPickService {

    private final UserPreferGenreMapper userPreferGenreMapper;
    private final MallangPickMapper mallangPickMapper;

    // 🔖 말랑이 고정 해시태그 풀
    private static final List<HashtagDTO> TAG_POOL = List.of(
            new HashtagDTO(1, "#이야기에빠지다"),
            new HashtagDTO(55889, "#마음이따뜻해지는"),
            new HashtagDTO(336, "#나를키우는시간"),
            new HashtagDTO(656, "#생각이깊어지는"),
            new HashtagDTO(74, "#세상을읽다"),
            new HashtagDTO(170, "#돈과인생이야기"),
            new HashtagDTO(48809, "#호기심폭발"),
            new HashtagDTO(517, "#취향저격"),
            new HashtagDTO(1143, "#꿈이자라는")
    );

    /* ===============================
       해시태그 3개 결정 로직
       =============================== */
    public List<HashtagDTO> pickHashtags(String userId) {

        // 1️⃣ 비로그인
        if (userId == null) {
            return randomPick(TAG_POOL, 3);
        }

        // 2️⃣ 로그인 → 선호 장르 조회
        List<Integer> preferIds =
                userPreferGenreMapper.findActiveGenreIdsByUserId(userId);

        // 3️⃣ 선호 없음
        if (preferIds == null || preferIds.isEmpty()) {
            return randomPick(TAG_POOL, 3);
        }

        // 4️⃣ 선호 있음
        List<HashtagDTO> preferred = TAG_POOL.stream()
                .filter(t -> preferIds.contains(t.getGenreId()))
                .toList();

        List<HashtagDTO> result =
                randomPick(preferred, Math.min(3, preferred.size()));

        // ⭐ 부족하면 랜덤으로 채움
        if (result.size() < 3) {
            List<HashtagDTO> remain = TAG_POOL.stream()
                    .filter(t -> result.stream()
                            .noneMatch(r -> r.getGenreId() == t.getGenreId()))
                    .toList();

            List<HashtagDTO> fill =
                    randomPick(remain, 3 - result.size());

            List<HashtagDTO> merged = new ArrayList<>(result);
            merged.addAll(fill);
            return merged;
        }

        return result;
    }

    /* ===============================
       도서 3권 추천
       =============================== */
    public List<MallangPickDTO> pickBooks(int genreId) {
        return mallangPickMapper.findRandomBooksByGenre(genreId, 3);
    }

    /* ===============================
       공통 랜덤 유틸
       =============================== */
    private <T> List<T> randomPick(List<T> src, int n) {
        List<T> copy = new ArrayList<>(src);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(n, copy.size()));
    }
}
