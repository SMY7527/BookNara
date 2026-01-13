package com.booknara.booknaraPrj.events.controller;

import com.booknara.booknaraPrj.events.dto.EventDTO;
import com.booknara.booknaraPrj.events.service.MainEventService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class MainEventController {

    private final MainEventService eventService;

    public MainEventController(MainEventService eventService) {
        this.eventService = eventService;
    }

    // ✅ 업로드 폴더(로컬)
    private final String uploadDir =
            System.getProperty("user.home") + File.separator + "booknara_uploads";

    // ✅ 이벤트 목록
    @GetMapping("/event/list")
    public String getEventsList(
            @RequestParam(value = "tab", defaultValue = "ongoing") String tab,
            Model model
    ) {
        int soonDays = 7;
        List<EventDTO> list = eventService.getEventListByTab(tab, soonDays);

        model.addAttribute("list", list);
        model.addAttribute("tab", tab);
        return "event/list";
    }

    // ✅ 이벤트 상세
    @GetMapping("/event/detail")
    public String getEvent(@RequestParam("id") String id, Model model) {
        EventDTO eventDTO = eventService.getEventDTOByID(id);

        //

        System.out.println("detail   eventdto" +   eventDTO);
        System.out.println("detail   eventdto images   : !!!" +   eventDTO.getImages());


        model.addAttribute("event", eventDTO);
        return "event/detail";
    }

    // ✅ 이벤트 등록 폼
    @GetMapping("/event/register")
    public String registerEvent() {
        return "event/eventForm";
    }

    /**
     * ✅ 이벤트 등록 (files 하나 + multiple)
     * - files[0] = THUMB
     * - files[1..] = DETAIL
     */
    @PostMapping("/event/register")
    public String registerEventSubmit(
            EventDTO eventDTO,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        // 로그(원하면 지워도 됨)
        System.out.println("eventDTO = " + eventDTO);
        System.out.println("files count = " + (files == null ? 0 : files.length));

        eventService.createEvent(eventDTO, files);

        // ✅ 등록 후 목록으로 보내는 게 일반적
        return "redirect:/event/list";
        // 만약 같은 페이지로 다시 띄우고 싶으면:
        // return "event/eventForm";
    }

    /**
     * ✅ /uploads/{filename} -> 로컬 폴더(booknara_uploads)에서 파일 제공
     * - Content-Type을 파일에 맞게 자동 추론해서 응답
     */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws MalformedURLException {

        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // ✅ Content-Type 자동 판별 (jpg/png 모두 대응)
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String probe = Files.probeContentType(filePath);
            if (probe != null) {
                mediaType = MediaType.parseMediaType(probe);
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
