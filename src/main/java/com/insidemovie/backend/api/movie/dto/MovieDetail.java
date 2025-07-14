package com.insidemovie.backend.api.movie.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetail {
    private Long id;
    private String title;
    private String overview; //줄거리
    private boolean adult; //성인(기본 값 true)
    private String original_language; //원어
    private int popularity; //인기
    private float vote_average; //평점
    private int vote_count;//관람수

    @JsonProperty("genre_ids")
    private List<Integer> genreIds; //장르

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("poster_path")
    private String posterPath;
}
