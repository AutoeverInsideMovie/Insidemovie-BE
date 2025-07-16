package com.insidemovie.backend.api.movie.dto.movie;

import com.insidemovie.backend.api.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieResponseDTO {
    private Long id;
    private Long tmdbMovieId;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private LocalDate releaseDate;
    private String originalLanguage;
    private List<Integer> genreIds;

    public static MovieResponseDTO fromEntity(Movie m) {
        return MovieResponseDTO.builder()
                .id(m.getId())
                .tmdbMovieId(m.getTmdbMovieId())
                .title(m.getTitle())
                .overview(m.getOverview())
                .posterPath(m.getPosterPath())
                .backdropPath(m.getBackdropPath())
                .voteAverage(m.getVoteAverage())
                .releaseDate(m.getReleaseDate())
                .originalLanguage(m.getOriginalLanguage())
                .genreIds(m.getGenreIds())
                .build();
    }
}
