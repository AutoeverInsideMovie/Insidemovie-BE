package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ReleaseDateResultDTO {
    @JsonProperty("iso_3166_1")
    private String iso3166_1;

    @JsonProperty("release_dates")
    private List<ReleaseDateDTO> releaseDates;
}
