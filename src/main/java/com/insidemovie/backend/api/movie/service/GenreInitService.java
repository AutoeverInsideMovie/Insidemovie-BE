package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.GenreDto;
import com.insidemovie.backend.api.movie.entity.Genre;
import com.insidemovie.backend.api.movie.repository.GenreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreInitService {
    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;

    @PostConstruct
    public void init() {
        List<GenreDto> allGenres = tmdbClient.fetchAllGenres();
        for (GenreDto dto : allGenres) {
            if (!genreRepository.existsByTmdbMovieId(dto.getId())) {
                Genre genre = Genre.builder()
                        .tmdbMovieId(dto.getId())
                        .genreNm(dto.getName())
                        .build();
                genreRepository.save(genre);
                log.info("Saved new genre: {} ({})", dto.getName(), dto.getId());
            }
        }
    }
}
