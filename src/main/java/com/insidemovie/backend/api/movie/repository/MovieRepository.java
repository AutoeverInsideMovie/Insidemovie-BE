package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbMovieId(Long id);
    List<Movie> findByTmdbMovieIdIn(Collection<Long> tmdbIds);
    List<Movie> findAllByTmdbMovieIdIn(Collection<Long> tmdbIds);
}
