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

    // 타임리프에서 item.filesJson으로 호출할 메서드
    public String getFilesJson() {
        List<Map<String, String>> fileList = new ArrayList<>();
        addFileToList(fileList, filePath1);
        addFileToList(fileList, filePath2);
        addFileToList(fileList, filePath3);

        try {
            return new ObjectMapper().writeValueAsString(fileList);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void addFileToList(List<Map<String, String>> list, String path) {
        if (path != null && !path.trim().isEmpty()) {
            Map<String, String> fileMap = new HashMap<>();

            // 경로 문자열에서 파일명만 뽑아냄 (예: "C:\download\error_screen.png" -> "error_screen.png")
            String fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
            if (fileName.contains("\\")) {
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }

            fileMap.put("url", "/download/" + fileName);
            fileMap.put("name", fileName);
            list.add(fileMap);
        }
    }
}