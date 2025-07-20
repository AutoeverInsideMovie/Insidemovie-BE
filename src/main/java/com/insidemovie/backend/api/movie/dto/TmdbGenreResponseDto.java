package com.insidemovie.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmdbGenreResponseDto { //tmdb에서 오는 장르 정보
    //tmdb ID
    private Long id;
    //장르 이름 "Action", "Drama"
    private String name;
}
