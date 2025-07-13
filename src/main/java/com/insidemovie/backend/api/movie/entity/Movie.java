package com.insidemovie.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Table(name = "movie")
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @Column(name = "kofic_id", unique = true)
    private String koficId;  // kofic 영화 코드

    @Column(name = "tmdb_id", unique = true)
    private String tmdbMovieId;  // tmdb 영화 코드

    private String title;
    private String titleEn;

    private Integer runtime;
    private String releaseDate;
    private Long audienceAcc;  // 누적 관객수

    private String nation;      // 제작 국가
    private String status;      // 제작 상태 (개봉, 기타 등)
    private String directors;



    @Column(columnDefinition = "TEXT")
    private String overview;

    private String rating;  // 관람 등급 (ALL, 12, 15, 18)

    private Boolean isOttAvailable;
    private String ottProviders;

    private String posterPath; // 포스터 이미지 경로
    private String backdropPath; // 배경 이미지 경로

    private Float voteAverage; // 평균 평점
    private Integer voteCount; // 평점 투표 수

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovieLike> movieLikes = new ArrayList<>();
}
