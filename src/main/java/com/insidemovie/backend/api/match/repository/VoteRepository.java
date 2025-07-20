package com.insidemovie.backend.api.match.repository;

import com.insidemovie.backend.api.match.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
