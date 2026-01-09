package com.booknara.booknaraPrj.bookSearch.controller;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import com.booknara.booknaraPrj.bookSearch.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/genres")
public class GenreController {

    private final GenreService service;

    @GetMapping("/parents")
    public List<GenreDTO> parents(@RequestParam(required = false) Integer top,
                                  @RequestParam(required = false) Integer min) {
        return service.parentsAutoWithForeign(top, min);
    }

    @GetMapping("/foreign/parents")
    public List<GenreDTO> foreignParents(@RequestParam(required = false) Integer top,
                                         @RequestParam(required = false) Integer min) {
        return service.foreignParentsTopWithEtc(top, min);
    }





    @GetMapping("/children")
    public List<GenreDTO> children(@RequestParam Integer parentId,
                                   @RequestParam(required = false) Integer top,
                                   @RequestParam(required = false) Integer min) {
        return service.childrenAuto(parentId, top, min);
    }
}

