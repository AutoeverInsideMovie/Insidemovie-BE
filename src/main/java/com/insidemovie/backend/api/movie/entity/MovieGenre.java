package com.insidemovie.backend.api.movie.entity;

import com.insidemovie.backend.api.constant.GenreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movie_genre")
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_genre_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(name="genre", length=20)
    private GenreType genreType;

    private MovieGenre(Long id, Movie movie, GenreType genreType) {
        this.id = id;
        this.movie = movie;
        this.genreType = genreType;
    }


}
