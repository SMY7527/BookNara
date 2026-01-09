package com.booknara.booknaraPrj.reviewstatus.controller;

import com.booknara.booknaraPrj.reviewstatus.dto.ReviewStatusDTO;
import com.booknara.booknaraPrj.reviewstatus.service.ReviewStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/reviewstatus")
public class ReviewStatusController {

    private final ReviewStatusService reviewStatusService;

    // 단건: /book/reviewstatus/{isbn13}
    @GetMapping("/{isbn13}")
    public ResponseEntity<ReviewStatusDTO> getOne(@PathVariable String isbn13) {
        ReviewStatusDTO dto = reviewStatusService.getByIsbn(isbn13);
        // 리뷰가 0이면 null 나올 수 있음 -> 프론트에서 0 처리하거나, 여기서 0 기본값 리턴해도 됨
        return ResponseEntity.ok(dto);
    }

    // 배치: /book/reviewstatus?isbns=xxx,yyy,zzz
    @GetMapping
    public ResponseEntity<List<ReviewStatusDTO>> getMany(@RequestParam(name = "isbns") String isbns) {
        List<String> list = Arrays.stream(isbns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (list.isEmpty()) return ResponseEntity.ok(List.of());


        return ResponseEntity.ok(reviewStatusService.getByIsbns(list));
    }
}
