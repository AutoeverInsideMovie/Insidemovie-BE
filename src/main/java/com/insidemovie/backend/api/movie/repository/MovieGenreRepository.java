package com.insidemovie.backend.api.movie.repository;

import com.insidemovie.backend.api.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    //특정 영화 장르 매핑이 존재하는 지 확인
    boolean existsByMovieIdAndGenreId(Long movieId, Long genreId);

    //특정 영화(movieId)에 매핑된 모든 MovieGenre 조회
    List<MovieGenre> findByMovieId(Long movieId);

    //특정 장르(genreId)에 매핑된 모든 MovieGenre 조회
    List<MovieGenre> findByGenreId(Long genreId);
}
