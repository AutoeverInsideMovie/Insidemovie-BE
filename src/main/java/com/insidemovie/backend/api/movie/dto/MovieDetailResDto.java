package com.insidemovie.backend.api.movie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetailResDto {
    private Long id; //tmdbid
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private String originalLanguage; //국가
    private String genre;


}
