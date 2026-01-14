package com.booknara.booknaraPrj.mypage.myinquiry;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
public class MyInquiryService {

    private final MyInquiryMapper inquiryMapper;

    public List<MyInquiryHistoryDto> getMyInquiry(String userId, String keyword) {
        return inquiryMapper.selectMyInquiry(userId, keyword);
    }

    /* ===============================
       문의 작성 + 파일 최대 3개
    =============================== */
    public void writeInquiry(MyInquiryWriteDto dto, List<MultipartFile> files) {

        // 1️⃣ INQ_ID 생성
        String inqId = "INQ_" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "_" +
                String.format("%03d", (int)(Math.random() * 1000));

        dto.setInqId(inqId);

        // 2️⃣ 문의 INSERT
        inquiryMapper.insertInquiry(dto);

        // 3️⃣ 파일 없으면 여기서 끝
        if (files == null || files.isEmpty()) {
            return;
        }

        // 4️⃣ 파일 저장 + 경로 수집
        String[] filePaths = new String[3];
        int idx = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            if (idx >= 3) break;

            filePaths[idx++] = saveFileAndReturnPath(file);
        }

        // 5️⃣ FILE_PATH_1~3 UPDATE
        int updated = inquiryMapper.updateInquiryFiles(
                inqId,
                filePaths[0],
                filePaths[1],
                filePaths[2]
        );


    }

    /* ===============================
       파일 저장
    =============================== */
    private String saveFileAndReturnPath(MultipartFile file) {
        try {
            String uploadDir = "C:/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // String savedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String savedName =   file.getOriginalFilename();
            File dest = new File(uploadDir + savedName);
            file.transferTo(dest);

            return savedName; // DB에 저장할 값

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
