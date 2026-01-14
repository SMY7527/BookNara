package com.booknara.booknaraPrj.report.dto;

import lombok.Data;

/**
 * [ReportCreateDTO]
 * 사용자로부터 새로운 신고 접수 요청을 받을 때 사용하는 데이터 전송 객체입니다.
 * 주로 리뷰 상세 화면의 '신고' 모달창에서 전송되는 데이터를 바인딩합니다.
 */
@Data
public class ReportCreateDTO {

    /** * [신고 대상 피드 ID]
     * 부적절한 내용이 포함된 리뷰(FEEDS 테이블)의 고유 식별자입니다.
     */
    private String feedId;

    /** * [신고 사유]
     * 사용자가 입력한 구체적인 신고 내용입니다.
     * (예: 욕설, 광고, 개인정보 노출 등)
     */
    private String reportContent;
}