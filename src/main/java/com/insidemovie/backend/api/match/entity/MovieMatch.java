package com.insidemovie.backend.api.match.entity;

import com.insidemovie.backend.api.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="movie_match")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_id")
    private Long id;

    @Column(name = "vote_count")
    private Long voteCount;

    @Column(name = "movie_rank")
    private Integer movieRank;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;
}