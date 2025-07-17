package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.movie.entity.Genre;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetailResDto {
    private Long id;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private String originalLanguage; //국가
    private List<String> genre;
}
