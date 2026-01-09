package com.booknara.booknaraPrj.admin.bookManagement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGenreService {

    private final AdminGenreRepository adminGenreRepository;

    public List<GenreResponseDTO> getAllGenres() {
        return adminGenreRepository.findAllByOrderByGenreNmAsc()
                .stream()
                .map(genre -> new GenreResponseDTO(
                        genre.getGenreId(),
                        genre.getGenreNm(),
                        genre.getParent() != null ? genre.getParent().getGenreNm() : null
                ))
                .collect(Collectors.toList());
    }
}

// 데이터를 전달하기 위한 간단한 DTO
record GenreResponseDTO(Integer genreId, String genreNm, String parentNm) {}