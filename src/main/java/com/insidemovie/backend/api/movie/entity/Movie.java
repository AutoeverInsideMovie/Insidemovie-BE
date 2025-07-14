package com.insidemovie.backend.api.movie.entity;

import com.insidemovie.backend.api.constant.EmotionType;
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

    private String title; //영화제목
    private String titleEn;

    private Integer runtime; //러닝타임
    private String releaseDate; //개봉일
    private Long audienceAcc;  // 누적 관객수

    private String nation;      // 제작 국가
    private String status;      // 제작 상태 (개봉, 기타 등)
    private String directors;  //감독



    @Column(columnDefinition = "TEXT")
    private String overview;

    private String rating;  // 관람 등급 (ALL, 12, 15, 18)

    private Boolean isOttAvailable;
    private String ottProviders;

    private String posterPath; // 포스터 이미지 경로
    private String backdropPath; // 배경 이미지 경로

    private Float voteAverage; // 평균 평점
    private Integer voteCount; // 평점 투표 수

    private String original_language; //국가
    private String actors; //출연진

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovieLike> movieLikes = new ArrayList<>();

    @Column(name = "dominant_emotion")
    @Enumerated(EnumType.STRING)
    private EmotionType dominantEmotion;  // 영화 대표 감정

}
