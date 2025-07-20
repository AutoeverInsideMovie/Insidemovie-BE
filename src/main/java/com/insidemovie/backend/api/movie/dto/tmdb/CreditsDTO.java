package com.insidemovie.backend.api.movie.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class CreditsDTO {
    private List<CastDTO> cast;
    private List<CrewDTO> crew;
}
