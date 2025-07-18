package com.insidemovie.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyMovieResponseDTO {
    private Long movieReactionId;
    private Long movieId;
}