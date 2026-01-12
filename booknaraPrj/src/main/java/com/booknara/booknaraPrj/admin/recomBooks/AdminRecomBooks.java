package com.booknara.booknaraPrj.admin.recomBooks;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECOM_BOOKS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 지연 로딩용
@AllArgsConstructor
@Builder
public class AdminRecomBooks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECOM_ID")
    private Integer recomId;

    @Column(name = "ISBN13", nullable = false, length = 20)
    private String isbn13;

    @Enumerated(EnumType.STRING) // DB에 문자열(active/inactive) 그대로 저장
    @Column(name = "STATE", nullable = false)
    @Builder.Default
    private AdminRecomState state = AdminRecomState.active;

    @CreationTimestamp // INSERT 시 현재 시간 자동 저장
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // UPDATE 시 현재 시간 자동 갱신
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // --- 비즈니스 로직 ---

    /**
     * 상태 변경 편의 메서드
     */
    public void changeState(AdminRecomState newState) {
        this.state = newState;
    }
}
