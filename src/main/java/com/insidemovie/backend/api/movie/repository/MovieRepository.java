package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByTmdbMovieIdIn(Collection<Long> tmdbIds);
    Optional<Movie> findByTmdbMovieId(Long tmdbMovieId);

    @Query("""
      SELECT m
      FROM Movie m
      WHERE LOWER(REPLACE(m.title, ' ', ''))
        LIKE LOWER(CONCAT('%', REPLACE(:title, ' ', ''), '%'))
    """)
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("""
    SELECT DISTINCT m
      FROM Movie m
      LEFT JOIN MovieGenre mg
        ON m = mg.movie
     WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
        OR mg.genreType IN :matchedGenres
  """)
    Page<Movie> searchByTitleOrGenre(
            @Param("q") String q,
            @Param("matchedGenres") List<GenreType> matchedGenres,
            Pageable pageable
    );

}
