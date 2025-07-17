package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetailDTO {
    private Integer runtime;
    private String status;
    private CreditsDTO credits;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("release_dates")
    private ReleaseDatesDTO releaseDates;

    @JsonProperty("watch/providers")
    private WatchProviderDTO watchProviders;

    @JsonProperty("original_title")
    private String originalTitle;
}
