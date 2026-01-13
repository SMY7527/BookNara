package com.booknara.booknaraPrj.login_signup.service;

import com.booknara.booknaraPrj.login_signup.mapper.UserPreferGenreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferGenreService {

    private final UserPreferGenreMapper mapper;

    // 회원가입 시 저장
    public void save(String userId, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return;

        for (Integer genreId : genreIds) {
            mapper.insert(userId, genreId);
        }
    }

    // ⭐ 조회 (로그인 / 마이페이지 공용)
    public List<Integer> getActiveGenres(String userId) {
        return mapper.findActiveGenreIdsByUserId(userId);
    }

    // 내정보 장르 수정
    @Transactional
    public void update(String userId, List<Integer> genreIds) {

        // 1️⃣ 기존 장르 전부 비활성화
        mapper.deactivateAllByUserId(userId);

        if (genreIds == null || genreIds.isEmpty()) {
            return; // 선택 안 해도 정상
        }

        // 2️⃣ 다시 활성화 or 신규 insert
        for (Integer genreId : genreIds) {
            int updated = mapper.activateGenre(userId, genreId);
            if (updated == 0) {
                mapper.insert(userId, genreId);
            }
        }
    }
}





