package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
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
    private MovieEmotionResDTO emotion;
}
