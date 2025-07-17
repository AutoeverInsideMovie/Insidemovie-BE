package com.insidemovie.backend.api.movie.dto.tmdb;

import lombok.Data;
import java.util.Map;

@Data
public class WatchProviderDTO {
    private Map<String, CountryProvidersDTO> results;
}
