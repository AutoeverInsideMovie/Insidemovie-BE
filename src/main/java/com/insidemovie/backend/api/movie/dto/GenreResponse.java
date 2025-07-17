package com.insidemovie.backend.api.movie.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GenreResponse {
    private List<GenreDto> genres;
}
