package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class MovieDetailSimpleResDto {
    private Long id;
    private String title;
    private String posterPath;
    private Double voteAverage;
    private MovieEmotionSummary emotion;
}
