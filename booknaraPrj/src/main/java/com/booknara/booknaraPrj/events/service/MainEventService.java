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
    private final ImageRepository imageRepository;   // ✅ 추가

    public MainEventService(EventRepository2 eventRepository, ImageRepository imageRepository) {
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
    }

    // ✅ 탭별 조회
    public List<EventDTO> getEventListByTab(String tab, int soonDays) {
        return switch (tab) {
            case "closing" -> eventRepository.findClosing(soonDays);
            case "closed" -> eventRepository.findClosed();
            case "ongoing" -> eventRepository.findOngoing();
            default -> eventRepository.findOngoing();
        };
    }

    public List<EventDTO> getEventList() {
        return eventRepository.findAll();
    }

    public EventDTO getEventDTOByID(String id) {
        return eventRepository.findById(id);
    }

    // ✅ 이벤트 등록 + 썸네일 저장
    public void createEvent(EventDTO dto, MultipartFile thumbFile) {

        // 1) EVENTS 저장 (eventId가 dto에 채워져야 함)
        eventRepository.insertEvent(dto);

        if (dto.getEventId() == null) {
            throw new IllegalStateException("eventId가 생성되지 않았습니다. insertEvent에 useGeneratedKeys 설정을 확인하세요.");
        }

        // 2) 파일 없으면 종료
        if (thumbFile == null || thumbFile.isEmpty()) return;

        // 3) MIME 타입 검사
        String contentType = thumbFile.getContentType();
        if (!("image/jpeg".equals(contentType) || "image/png".equals(contentType))) {
            throw new IllegalArgumentException("jpg, png 이미지만 업로드 가능합니다.");
        }

        // 4) 파일명/확장자 검사
        String originalName = thumbFile.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String original = StringUtils.cleanPath(originalName);
        int dot = original.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extWithDot = original.substring(dot).toLowerCase(); // ".jpg" / ".png"
        String ext = extWithDot.substring(1); // "jpg" / "png"

        if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
            throw new IllegalArgumentException("jpg, png 파일만 업로드 가능합니다.");
        }

        // 5) 파일 저장
        String savedName = UUID.randomUUID() + extWithDot;

        String uploadDir = System.getProperty("user.home") + File.separator + "booknara_uploads";
        new File(uploadDir).mkdirs();

        File dest = new File(uploadDir, savedName);
        try {
            thumbFile.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("썸네일 파일 저장 실패", e);
        }

        // 6) IMAGES insert
        String imgUrl = "/uploads/" + savedName;
        imageRepository.insertEventThumb(dto.getEventId(), imgUrl);
    }
}