package com.insidemovie.backend.api.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchMovieWrapperDTO {
    private int page;

    //Json의 "results"배열을 List<TmdbMovieDto>로 매핑
    private List<SearchMovieResponseDTO> results;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private int totalResults;
}
