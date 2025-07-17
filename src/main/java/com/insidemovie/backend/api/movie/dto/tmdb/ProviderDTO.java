package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProviderDTO {
    @JsonProperty("provider_id")
    private Integer providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("logo_path")
    private String logoPath;
}
