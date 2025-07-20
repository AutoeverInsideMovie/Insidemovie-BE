package com.insidemovie.backend.api.match.repository;

import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.match.entity.MovieMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieMatchRepository extends JpaRepository<MovieMatch, Long> {

}
