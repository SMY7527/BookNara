package com.booknara.booknaraPrj.admin.users;

import com.booknara.booknaraPrj.notification.dto.NotificationEntity;
import com.booknara.booknaraPrj.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 전체 유저 리스트 조회
     */
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 키워드로 유저 검색
     */

    public Page<Users> getPagedUsers(String keyword, String state, int page) {
        Pageable pageable = PageRequest.of(page, 50, Sort.by("createdAt").descending());

        // 1. 상태 카드를 눌렀을 때
        if (state != null && !state.isEmpty()) {
            if (state.equals("all")) {
                // ✅ 탈퇴자(4)를 제외한 모든 회원 조회
                // Repository에 findByUserStateNot("4", pageable) 메서드가 필요합니다.
                return userRepository.findByUserStateNot("4", pageable);
            }
            // 개별 상태(1, 2, 3, 4) 조회
            return userRepository.findByUserState(state, pageable);
        }

        // 2. 검색창 이용 시 (검색 시에도 탈퇴자를 빼고 싶다면 여기서 처리 가능)
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.searchUsers(keyword, pageable);
        }

        // 3. 기본 접근 (파라미터 없을 때) - 여기도 탈퇴자를 빼고 싶다면 수정
        return userRepository.findByUserStateNot("4", pageable);
    }

    /**
     * 상태별 회원 통계 계산
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();

        // 각 상태별 인원 조회
        long active = userRepository.countByUserState("1");
        long dormant = userRepository.countByUserState("2");
        long banned = userRepository.countByUserState("3");
        long withdrawn = userRepository.countByUserState("4");

        // ✅ 전체 회원수에서 탈퇴자(4) 제외 (1+2+3의 합)
        long totalExceptWithdrawn = active + dormant + banned;

        stats.put("total", totalExceptWithdrawn);
        stats.put("active", active);
        stats.put("dormant", dormant);
        stats.put("banned", banned);
        stats.put("withdrawn", withdrawn);

        return stats;
    }

    @Transactional
    public void updateUserState(String userId, String newState) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. ID: " + userId));

        user.setUserState(newState); // 더티 체킹에 의해 자동 업데이트

        System.out.println(newState);
        if(newState.equals("3")) {
            // 문의 답장 알림 저장
            NotificationEntity notiEntity = new NotificationEntity();
            notiEntity.setUserId(userId);
            notiEntity.setTargetType("ACCOUNT_RESTRICTED");
            notiEntity.setTargetId(null);
            notiEntity.setNotiContent("정책 위반으로 계정이 제한되었습니다.");
            notiEntity.setCheckYn('N');
            notificationService.saveNotification(notiEntity);
        }
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
