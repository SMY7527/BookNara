package com.booknara.booknaraPrj.admin.users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 전체 유저 리스트 조회
     */
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 키워드로 유저 검색
     */
    public List<Users> searchUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByUserIdContainingOrUserNmContainingOrEmailContaining(keyword, keyword, keyword);
    }

    /**
     * 상태별 회원 통계 계산
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", userRepository.count());
        stats.put("active", userRepository.countByUserState("1"));
        stats.put("dormant", userRepository.countByUserState("2"));
        stats.put("banned", userRepository.countByUserState("3"));
        return stats;
    }

    @Transactional
    public void updateUserState(String userId, String newState) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. ID: " + userId));

        user.setUserState(newState); // 더티 체킹에 의해 자동 업데이트
    }

    @Transactional
    public void updateNickname(String userId, String userNm) {
        // 1. 유저 찾기
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

        // 2. 닉네임(이름) 변경
        user.setUserNm(userNm);

        // 3. @Transactional에 의해 메서드 종료 시 자동 flush(DB 반영)
    }
}
