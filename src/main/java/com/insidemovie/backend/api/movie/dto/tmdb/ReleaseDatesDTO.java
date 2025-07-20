package com.insidemovie.backend.api.movie.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class ReleaseDatesDTO {
    private List<ReleaseDateResultDTO> results;
}
