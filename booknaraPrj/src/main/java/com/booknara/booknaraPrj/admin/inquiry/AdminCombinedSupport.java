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
    @Column(name = "TOTAL_ID")
    private String totalId;

    @Column(name = "ORIGINAL_ID")
    private String originalId;

    private String type;
    private String subType;
    private String title;
    private String userId;
    private String state;
    private LocalDateTime regDate;
    private LocalDateTime resolvedAt;

    @Lob
    private String content;

    @Lob
    private String answer;

    // View에서 가져온 파일 경로 매핑
    @Column(name = "FILE_PATH_1")
    private String filePath1;
    @Column(name = "FILE_PATH_2")
    private String filePath2;
    @Column(name = "FILE_PATH_3")
    private String filePath3;

    private static final ObjectMapper mapper = new ObjectMapper();

    public String fetchFilesJson() { // 이름을 get 대신 다른 것으로 시작하게 변경 (충돌 방지)
        List<Map<String, String>> fileList = new ArrayList<>();

        try {
            addFileToList(fileList, filePath1);
            addFileToList(fileList, filePath2);
            addFileToList(fileList, filePath3);

            if (fileList.isEmpty()) return "[]";
            return mapper.writeValueAsString(fileList);
        } catch (Exception e) {
            return "[]"; // 에러 발생 시 빈 JSON 배열 반환
        }
    }

    private void addFileToList(List<Map<String, String>> list, String path) {
        if (path != null && !path.trim().isEmpty()) {
            Map<String, String> fileMap = new HashMap<>();

            // 경로 처리 로직은 유지하되 null 방지 추가
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