package com.booknara.booknaraPrj.admin.inquiry;

import com.booknara.booknaraPrj.admin.users.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "INQUIRY")
@Data
public class AdminInquiry {
    @Id
    @Column(name = "INQ_ID")
    private String inqId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users user;

    @Column(name = "INQ_TITLE")
    private String inqTitle;

    @Column(name = "INQ_TYPE")
    private Integer inqType; // DB 타입이 TINYINT이므로 Integer로 변경

    @Column(name = "INQ_CONTENT")
    private String inqContent;

    @Column(name = "RESP_STATE")
    private String respState;

    @Column(name = "RESP_CONTENT")
    private String respContent;

    @Column(name = "RESP_AT")
    private LocalDateTime respAt;

    @Column(name = "RESP_USER_ID")
    private String respUserId;

    @Column(name = "FILE_PATH_1")
    private String filePath1;

    @Column(name = "FILE_PATH_2")
    private String filePath2;

    @Column(name = "FILE_PATH_3")
    private String filePath3;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // 유형 이름을 반환하는 헬퍼 메서드
    public String getInqTypeName() {
        if (this.inqType == null) return "기타";
        return switch (this.inqType) {
            case 1 -> "도서관리";
            case 2 -> "대여/반납";
            case 3 -> "시스템";
            case 4 -> "계정";
            case 5 -> "기타";
            default -> "기타";
        };
    }

    // [중요] 파일 경로들을 JSON 형태로 변환하여 화면으로 전달
    public String getFilesJson() {
        List<Map<String, String>> fileList = new ArrayList<>();
        addFileToList(fileList, filePath1);
        addFileToList(fileList, filePath2);
        addFileToList(fileList, filePath3);

        try {
            return new ObjectMapper().writeValueAsString(fileList);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private void addFileToList(List<Map<String, String>> list, String path) {
        if (path != null && !path.isEmpty()) {
            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("url", path);
            // 파일명만 추출 (예: /upload/test.jpg -> test.jpg)
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            fileMap.put("name", fileName);
            list.add(fileMap);
        }
    }
}