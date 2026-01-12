package com.booknara.booknaraPrj.admin.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminNotiResponseDto {
    private Long notiId;
    private String notiType;
    private String notiContent;
    private String userId;
    private String userNm;
    private String lendId;
    private String bookTitle;
    private String isbn13;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
