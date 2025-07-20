package com.insidemovie.backend.api.match.repository;

import com.insidemovie.backend.api.match.entity.MovieMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieMatchRepository extends JpaRepository<MovieMatch, Long> {
    List<MovieMatch> findByMatchId(Long matchId);

    // 매치 + 영화 검색
    Optional<MovieMatch> findByMatchIdAndMovieId(Long matchId, Long movieId);
}
