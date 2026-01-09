package com.booknara.booknaraPrj.bookSearch.service;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import com.booknara.booknaraPrj.bookSearch.mapper.GenreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreMapper mapper;

    public List<GenreDTO> parentsAuto(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 20 : top;
        int m = (min == null || min < 0) ? 0 : min;
        return mapper.selectParentGenresAuto(t, m);
    }

    public List<GenreDTO> childrenAuto(Integer parentId, Integer top, Integer min) {
        if (parentId == null || parentId <= 0) return List.of();
        int t = (top == null || top <= 0) ? 30 : top;
        int m = (min == null || min < 0) ? 0 : min;
        return mapper.selectChildGenresAuto(parentId, t, m);
    }

    public List<GenreDTO> parentsAutoWithForeign(Integer top, Integer min) {
        List<GenreDTO> list = parentsAuto(top, min);

        GenreDTO foreign = new GenreDTO();
        foreign.setGenreId(null);
        foreign.setGenreNm("외국도서");
        foreign.setParentId(null);
        foreign.setMall("외국도서");

        list.add(foreign);
        return list;
    }

    public List<GenreDTO> foreignParentsTopWithEtc(Integer top, Integer min) {
        int t = (top == null || top <= 0) ? 19 : top; // ✅ Top 19
        int m = (min == null || min < 0) ? 0 : min;

        List<GenreDTO> list = mapper.selectForeignParentGenresAuto(t, m);

        GenreDTO etc = new GenreDTO();
        etc.setGenreId(-1);               // ✅ '기타'는 -1
        etc.setGenreNm("기타");
        etc.setParentId(null);
        etc.setMall("외국도서");

        list.add(etc); // ✅ 20번째
        return list;
    }



}


