package com.insidemovie.backend.api.movie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendedMovieReqDto {
    private String genre;
}
