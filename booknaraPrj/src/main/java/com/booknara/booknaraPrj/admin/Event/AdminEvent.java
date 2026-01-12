package com.booknara.booknaraPrj.admin.Event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "EVENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "EVENT_TITLE", nullable = false, length = 300)
    private String eventTitle;

    @Lob
    @Column(name = "EVENT_CONTENT", nullable = false)
    private String eventContent;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Lob
    @Column(name = "EVENT_EDIT")
    private String eventEdit;

    @Builder.Default
    @Column(name = "EVENT_MAIN_YN", nullable = false, length = 1)
    private String eventMainYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}