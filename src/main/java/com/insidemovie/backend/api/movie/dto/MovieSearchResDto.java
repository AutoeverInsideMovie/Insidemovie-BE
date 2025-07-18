package com.insidemovie.backend.api.movie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieSearchResDto {
    private Long id;
    private String posterPath;
    private String title;
    private Double voteAverage;
}
