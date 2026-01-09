package com.booknara.booknaraPrj.admin.report;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
public class AdminReportRequestDto {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotNull(message = "신고 유형(FEED/COMMENT)을 선택해주세요.")
    private AdminReportType adminReportType;

    @NotBlank(message = "신고 내용을 입력해주세요.")
    @Size(max = 500, message = "신고 내용은 500자 이내로 작성해주세요.")
    private String reportContent;
}
