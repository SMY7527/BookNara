package com.booknara.booknaraPrj.report.dto;

import lombok.Data;

/**
 * [ReportCheckDTO]
 * 특정 유저가 특정 피드를 신고했는지 여부를 조회하기 위한 요청 데이터 객체입니다.
 * 중복 신고 방지 로직의 파라미터로 사용됩니다.
 */
@Data
public class ReportCheckDTO {

    /** 신고 여부를 확인할 사용자 ID (현재 로그인한 유저) */
    private String userId;

    /** 신고 대상이 되는 피드(리뷰)의 고유 식별자 */
    private String feedId;
}