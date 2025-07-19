package com.insidemovie.backend.api.movie.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendedMovieResDto {
    private Long id;
    private String posterPath;
    private String title;
    private Double voteAverage;

    private LocalDate releaseDate;       // 개봉일
}
