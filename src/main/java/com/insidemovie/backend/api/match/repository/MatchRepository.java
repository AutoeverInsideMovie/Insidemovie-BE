package com.insidemovie.backend.api.match.repository;

import com.insidemovie.backend.api.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    // 최근 매치
    Optional<Match> findTopByOrderByMatchNumberDesc();
}
