package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class MovieDetailSimpleResDto {
    private Long id;
    private String title;
    private String posterPath;
    private Double voteAverage;
    private MovieEmotionResDTO emotion;
    private BigDecimal ratingAvg;
}
