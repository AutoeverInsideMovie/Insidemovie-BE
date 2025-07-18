package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre,Long> {
    boolean existsByTmdbMovieId(Long tmdbId); //존재여부 체크
    Optional<Genre> existsByGenreNm(String genre);
}
