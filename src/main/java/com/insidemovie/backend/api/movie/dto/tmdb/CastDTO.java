package com.insidemovie.backend.api.movie.dto.tmdb;

import lombok.Data;

@Data
public class CastDTO {
    private Integer id;
    private String name;
    private String character;
}
