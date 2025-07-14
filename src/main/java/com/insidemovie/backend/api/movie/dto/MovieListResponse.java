package com.insidemovie.backend.api.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieListResponse {
    private MovieListResult movieListResult;
    @Data
    public static class MovieListResult {
        private int totCnt;
        private List<Movie> movieList;
    }
    @Data
    public static class Movie{
        @JsonProperty("movieCd")
        private String id;
        private String movieNm; //영화 명
        //private String showTm; //상영시간
        private String openDt; //개봉일
        private String nationAlt; //제작국가
        private String peopleNm; //감독
    }

}
