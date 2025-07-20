package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchMovieResponseDTO {
    private Long id;
    private String title;
    private String overview; //줄거리

    @JsonProperty("poster_path")
    private String posterPath;          // 포스터

    @JsonProperty("backdrop_path")
    private String backDropPath;        // 배경 이미지

    @JsonProperty("vote_average")
    private Double voteAverage;         // 평균 평점

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("release_date")
    private LocalDate releaseDate;      // 개봉일

//    @JsonProperty("genre_ids")
//    private List<Integer> genreIds;     //장르

    @JsonProperty("original_language")
    private String originalLanguage;    // 국가

    @JsonProperty("popularity")
    private Double popularity;          // 인기

    @JsonProperty("adult")
    private Boolean adult;
}
