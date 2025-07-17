package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReleaseDateDTO {
    private String certification;
    private String type;
    @JsonProperty("release_date")
    private String releaseDate;
}
