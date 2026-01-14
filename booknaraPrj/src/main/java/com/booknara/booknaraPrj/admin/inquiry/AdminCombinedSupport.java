package com.booknara.booknaraPrj.admin.inquiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Immutable
@Table(name = "VIEW_COMBINED_SUPPORT")
@Getter
@Setter
public class AdminCombinedSupport {

    @Id
    @Column(name = "total_id") // View 컬럼명: total_id
    private String totalId;

    @Column(name = "original_id") // View 컬럼명: original_id
    private String originalId;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type") // View 컬럼명: sub_type
    private String subType;

    @Column(name = "title")
    private String title;

    @Column(name = "user_id") // View 컬럼명: user_id
    private String userId;

    @Column(name = "state")
    private String state;

    @Column(name = "reg_date") // View 컬럼명: reg_date
    private LocalDateTime regDate;

    @Column(name = "resolved_at") // View 컬럼명: resolved_at
    private LocalDateTime resolvedAt;

    @Lob
    @Column(name = "content")
    private String content;

    @Lob
    @Column(name = "answer")
    private String answer;

    // View에서 가져온 파일 경로 매핑 (SQL 대소문자 구분 없이 매핑되나 명시적 작성)
    @Column(name = "FILE_PATH_1")
    private String filePath1;

    @Column(name = "FILE_PATH_2")
    private String filePath2;

    @Column(name = "FILE_PATH_3")
    private String filePath3;

    @Transient // DB 컬럼이 아님을 명시
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 파일 경로들을 JSON 형태로 변환하여 반환
     */
    public String fetchFilesJson() {
        List<Map<String, String>> fileList = new ArrayList<>();
        try {
            addFileToList(fileList, filePath1);
            addFileToList(fileList, filePath2);
            addFileToList(fileList, filePath3);

            if (fileList.isEmpty()) return "[]";
            return mapper.writeValueAsString(fileList);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void addFileToList(List<Map<String, String>> list, String path) {
        if (path != null && !path.trim().isEmpty()) {
            Map<String, String> fileMap = new HashMap<>();
            String fileName = "unknown_file";
            try {
                fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
                if (fileName.contains("\\")) {
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                }
            } catch (Exception e) {
                fileName = "file_" + System.currentTimeMillis();
            }

            fileMap.put("url", "/download/" + fileName);
            fileMap.put("name", fileName);
            list.add(fileMap);
        }
    }
}