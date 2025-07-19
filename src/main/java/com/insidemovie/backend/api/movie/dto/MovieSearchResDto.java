package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MovieSearchResDto {
    private Long id;
    private String posterPath;
    private String title;
    private Double voteAverage;
    private EmotionType mainEmotion;
}
