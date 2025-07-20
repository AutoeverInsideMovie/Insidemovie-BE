package com.insidemovie.backend.api.match.entity;

import com.insidemovie.backend.api.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="movie_match")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class MovieMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_id")
    private Long id;

    @Column(name = "vote_count")
    private Long voteCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;
}