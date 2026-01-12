package com.booknara.booknaraPrj.admin.bookManagement;

import lombok.*;

@Getter
@Setter // Form 데이터를 바인딩하기 위해 Setter가 필요할 수 있습니다.
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookSaveRequestDto {
    private String isbn13;
    private String bookTitle;
    private String authors;
    private String publisher;
    private String pubDate;
    private String description;
    private String naverImage;
    private String aladinImageBig;
    private String eBookYn; // "Y" 또는 "N"

    // 핵심: 장르 이름이 아닌 ID를 받아야 합니다 (HTML의 hidden input 값)
    private Integer genreId;

    // 기본값 설정이 필요한 필드들 (필요시)
    private String bookState; // 기본값 'N'
}