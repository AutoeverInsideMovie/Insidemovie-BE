package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieEmotionSummaryRepository extends JpaRepository<MovieEmotionSummary, Long> {
    Optional<MovieEmotionSummary> findByMovieId(Long movieId);
    Optional<MovieEmotionSummary> findByMovie(Movie movie);
}
