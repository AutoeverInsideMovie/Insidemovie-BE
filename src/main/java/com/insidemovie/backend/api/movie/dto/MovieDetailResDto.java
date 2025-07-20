package com.insidemovie.backend.api.movie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDetailResDto {
    private Long id;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private String originalLanguage; // 국가
    private Boolean isLike; // 좋아요 여부
    private List<String> genre;


    private String actors;      //영화 배우
    private String director;    //감독
    private String ottProviders; //ott 여부
    private String rating;      //연령
    private LocalDateTime releaseDate;  //개봉일
    private Integer runtime;    //러닝타임
    private String status;      //제작 및 배급 상태
    private String titleEn;     //영화 영문 이름


}
