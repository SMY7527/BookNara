package com.booknara.booknaraPrj.events.service;

import com.booknara.booknaraPrj.events.dto.EventDTO;
import com.booknara.booknaraPrj.events.repository.EventRepository2;
import com.booknara.booknaraPrj.events.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class MainEventService {

    private final EventRepository2 eventRepository;
    private final ImageRepository imageRepository;

    // ✅ 업로드 폴더(로컬)
    private final String uploadDir =
            System.getProperty("user.home") + File.separator + "booknara_uploads";

    public MainEventService(EventRepository2 eventRepository, ImageRepository imageRepository) {
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
    }

    // ✅ 탭별 조회
    public List<EventDTO> getEventListByTab(String tab, int soonDays) {
        return switch (tab) {
            case "closing" -> eventRepository.findClosing(soonDays);
            case "closed"  -> eventRepository.findClosed();
            case "ongoing" -> eventRepository.findOngoing();
            default        -> eventRepository.findOngoing();
        };
    }

    public List<EventDTO> getEventList() {
        return eventRepository.findAll();
    }

    public EventDTO getEventDTOByID(String id) {
        return eventRepository.findById(id);
    }

    /**
     * ✅ 이벤트 등록 + 이미지들 저장
     * 규칙:
     * files[0] -> THUMB
     * files[1] -> MAIN
     * files[2..] -> CONTENT
     */
    public void createEvent(EventDTO dto, MultipartFile[] files) {

        // 1) EVENTS 저장 (eventId 채워져야 함: useGeneratedKeys)
        eventRepository.insertEvent(dto);

        if (dto.getEventId() == null) {
            throw new IllegalStateException("eventId가 생성되지 않았습니다. insertEvent useGeneratedKeys/keyProperty 확인!");
        }

        // 2) 파일 없으면 종료
        if (files == null || files.length == 0) return;

        // 3) 저장 폴더 준비
        new File(uploadDir).mkdirs();

        // 4) 파일 순서대로 저장 + DB insert
        for (int i = 0; i < files.length; i++) {
            MultipartFile f = files[i];
            if (f == null || f.isEmpty()) continue;

            // ✅ (중요) MIME 타입 체크
            String contentType = f.getContentType();
            if (!("image/jpeg".equals(contentType) || "image/png".equals(contentType))) {
                throw new IllegalArgumentException("jpg/png 이미지만 업로드 가능합니다.");
            }

            // ✅ 확장자 체크
            String originalName = f.getOriginalFilename();
            if (originalName == null) throw new IllegalArgumentException("파일명이 없습니다.");

            String clean = StringUtils.cleanPath(originalName);
            int dot = clean.lastIndexOf('.');
            if (dot < 0) throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");

            String extWithDot = clean.substring(dot).toLowerCase(); // ".jpg" ".png"
            String ext = extWithDot.substring(1);                   // "jpg" "png"

            if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
                throw new IllegalArgumentException("jpg/png 파일만 업로드 가능합니다.");
            }

            // ✅ 파일 저장명
            String savedName = UUID.randomUUID() + extWithDot;
            File dest = new File(uploadDir, savedName);

            try {
                f.transferTo(dest);
            } catch (Exception e) {
                throw new RuntimeException("이미지 파일 저장 실패", e);
            }

            // ✅ 브라우저에서 접근할 보여줄 URL
            String imgUrl = "/uploads/" + savedName;

            // ✅ IMG_TYPE 결정
            String imgType;
            if (i == 0) imgType = ImageRepository.TYPE_THUMB;
            else if (i == 1) imgType = ImageRepository.TYPE_MAIN;
            else imgType = ImageRepository.TYPE_CONTENT;

            // ✅ DB insert
            imageRepository.insertEventImage(dto.getEventId(), imgUrl, imgType);
        }
    }
}
