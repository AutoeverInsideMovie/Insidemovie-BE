package com.insidemovie.backend.api.movie.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MovieSearchReqDto {
    private String title;
    private String genre;
}
