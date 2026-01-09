package com.booknara.booknaraPrj.notification.repository;

import com.booknara.booknaraPrj.notification.dto.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Optional<NotificationEntity> findByNotiIdAndUserId(Long notiId, String userId);

    List<NotificationEntity> findByUserIdAndCheckYnAndTargetTypeIn(
            String userId, char checkYn, List<String> targetTypes
    );

    // ALL 탭용
    List<NotificationEntity> findByUserIdAndCheckYn(String userId, char checkYn);
}
