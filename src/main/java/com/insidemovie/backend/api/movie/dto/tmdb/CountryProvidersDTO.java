package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CountryProvidersDTO {
    private String link;
    private List<ProviderDTO> providers;
    private List<ProviderDTO> rent;
    private List<ProviderDTO> buy;
    @JsonProperty("flatrate")
    private List<ProviderDTO> flatrate;
}
