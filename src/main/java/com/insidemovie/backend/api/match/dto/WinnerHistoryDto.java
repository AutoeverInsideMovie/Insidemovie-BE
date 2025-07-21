package com.insidemovie.backend.api.match.dto;

import com.insidemovie.backend.api.movie.dto.MovieDetailSimpleResDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class WinnerHistoryDto {
    private int matchNumber;
    private LocalDate matchDate;
    private MovieDetailSimpleResDto movie;
}
