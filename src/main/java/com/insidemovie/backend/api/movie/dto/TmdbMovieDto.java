package com.insidemovie.backend.api.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TmdbMovieDto {
    private Long id;
    private String title;
    private String overview; //줄거리

    @JsonProperty("poster_path")
    private String posterPath; //포스터

    @JsonProperty("backdrop_path")
    private String backDropPath; //배경 이미지

    @JsonProperty("vote_average")
    private Double voteAverage; //평균 평점

    @JsonProperty("release_date")
    private LocalDate releaseDate; //개봉일

    @JsonProperty("genre_ids")
    private List<Integer> genreIds; //장르

    @JsonProperty("original_language")
    private String originalLanguage; //국가

    //등급
    //러닝타임
    //관객수
    //감독이름
    //출연진
    //가능 ott
}
