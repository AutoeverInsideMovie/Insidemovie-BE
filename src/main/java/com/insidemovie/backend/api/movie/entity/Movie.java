package com.insidemovie.backend.api.movie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@Table(name = "movie")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"movieLikes", "emotions"})
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @Column(name = "kofic_id", unique = true)
    private String koficId;             // kofic 영화 코드

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbMovieId;            // tmdb 영화 코드

    @Lob
//    @Column(columnDefinition = "TEXT")
    private String overview;            // 영화 개요

    @Column(name = "popularity")
    private Double popularity;          // tmdb 자체 인기도

    @Column(name = "original_language")
    private String originalLanguage;     // 국가

    @Lob
    private String actors;              // 출연진

//    @Column(name = "genre_ids")
//    private Set<Integer> genreIds;     // 장르

    private String title;                // 영화 제목
    private String titleEn;              // 영문 영화 제목
    private Integer runtime;             // 러닝 타임
    private String nation;               // 제작 국가
    private String status;               // 제작 상태 (개봉, 기타 등)
    @Lob
    private String directors;            // 감독
    private String ottProviders;         // OTT 제공
    private String posterPath;           // 포스터 이미지 경로
    private String backdropPath;         // 배경 이미지 경로
    private Double voteAverage;          // 평균 평점
    private Integer voteCount;           // 평점 투표 수
    private String rating;               // 영화 등급
    private LocalDate releaseDate;       // 개봉일

    @Column(name = "is_matched")
    private Boolean isMatched;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovieLike> movieLikes = new ArrayList<>();

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private MovieEmotionSummary emotions;

    // 제목 수정
    public void updateTitle(String title) {
        this.title = title;
    }
    // 개요 수정
    public void updateOverview(String overview) {
        this.overview = overview;
    }
    // 포스터 수정
    public void updatePosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    // 배경이미지 수정
    public void updateBackDropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
    // 평균평점 수정
    public void updateVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }
    // 장르 수정
    //public void updateGenreIds(List<Long> genreIds) {this.genreIds = genreIds;}
    // 국가 수정
    public void updateOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }
    // 개봉일 수정
    public void updateReleaseDate(LocalDate date) {
        this.releaseDate = date;
    }
    // 인기 수정
    public void updatePopularity(Double popularity) {
        this.popularity = popularity;
    }
}
