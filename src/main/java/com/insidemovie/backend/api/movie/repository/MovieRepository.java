package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
