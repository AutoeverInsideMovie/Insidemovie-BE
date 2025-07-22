package com.insidemovie.backend.api.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class MovieSimilarityResDto {
    @JsonProperty("movie_id")
    private Long movieId;
    private Float similarity;
}
