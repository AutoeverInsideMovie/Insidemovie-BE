package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insidemovie.backend.api.movie.dto.TmdbGenreResponseDto;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetailDTO {
    private Integer runtime;
    private String status;
    private CreditsDTO credits;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("release_dates")
    private ReleaseDatesDTO releaseDates;

    @JsonProperty("watch/providers")
    private WatchProviderDTO watchProviders;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("poster_path")
    private String posterPath;             // 포스터 URL 조각

    @JsonProperty("backdrop_path")
    private String backdropPath;           // 배경 이미지 URL 조각

    @JsonProperty("vote_average")
    private Double voteAverage;            // 평균 평점

    @JsonProperty("original_language")
    private String originalLanguage;       // 언어 코드

    @JsonProperty("popularity")
    private Double popularity;             // 인기 지표

    @JsonProperty("genres")
    private List<TmdbGenreResponseDto> genres;         // GenreDto 는 tmdb 패키지에 선언된 클래스여야 합니다.

    @JsonProperty("release_date")
    private LocalDate releaseDate;         // 개봉일

    private String title;                // 제목
    private String overview;             // 줄거리
}
