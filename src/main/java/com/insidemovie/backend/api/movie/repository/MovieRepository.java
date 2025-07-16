package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByTmdbMovieIdIn(Collection<Long> tmdbIds);

    Optional<Movie> findByTmdbMovieId(Long tmdbMovieId);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    boolean existsByTmdbMovieId(Long tmdbMovieId);
}
