package com.booknara.booknaraPrj.bookSearch.controller;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import com.booknara.booknaraPrj.bookSearch.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [GenreController]
 * 도서 카테고리(장르) 데이터를 제공하는 REST 컨트롤러입니다.
 * 모든 메서드는 JSON 형태의 리스트(List<GenreDTO>)를 반환합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/genres")
public class GenreController {

    private final GenreService service;

    /**
     * [국내도서 상위 장르 목록 조회]
     * - URL: /book/genres/parents
     * - 특징: 국내도서 인기 장르 목록 뒤에 '외국도서' 진입점 메뉴를 하나 더 붙여서 반환합니다.
     * @param top 추출할 상위 장르 개수 (기본값: 20)
     * @param min 노출 기준 최소 도서 권수 (기본값: 0)
     */
    @GetMapping("/parents")
    public List<GenreDTO> parents(@RequestParam(required = false) Integer top,
                                  @RequestParam(required = false) Integer min) {
        return service.parentsAutoWithForeign(top, min);
    }

    /**
     * [외국도서 상위 장르 목록 조회]
     * - URL: /book/genres/foreign/parents
     * - 특징: 외국도서의 상위 장르들과 함께, 나머지 장르들을 묶은 가상의 '기타' 메뉴를 반환합니다.
     */
    @GetMapping("/foreign/parents")
    public List<GenreDTO> foreignParents(@RequestParam(required = false) Integer top,
                                         @RequestParam(required = false) Integer min) {
        return service.foreignParentsTopWithEtc(top, min);
    }

    /**
     * [하위 장르 목록 조회]
     * - URL: /book/genres/children
     * - 역할: 특정 부모 장르를 클릭하거나 호버했을 때 노출될 상세 카테고리 목록을 반환합니다.
     * @param parentId 부모 장르의 ID (필수)
     */
    @GetMapping("/children")
    public List<GenreDTO> children(@RequestParam Integer parentId,
                                   @RequestParam(required = false) Integer top,
                                   @RequestParam(required = false) Integer min) {
        return service.childrenAuto(parentId, top, min);
    }
}