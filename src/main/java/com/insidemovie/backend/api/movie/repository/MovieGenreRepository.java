package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {

    @Modifying
    @Transactional
    void deleteByMovie(Movie movie);

    //특정 영화(movieId)에 매핑된 모든 MovieGenre 조회
    List<MovieGenre> findByMovieId(Long movieId);
    Page<MovieGenre> findByGenreTypeIn(List<GenreType> genreType, Pageable pageable);
    Page<MovieGenre> findByGenreType(GenreType genreType, Pageable pageable);

    List<MovieGenre> findByMovie(Movie movie);
}
