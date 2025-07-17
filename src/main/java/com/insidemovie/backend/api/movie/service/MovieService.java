package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.TmdbMovieDto;
import com.insidemovie.backend.api.movie.dto.TmdbResponse;
import com.insidemovie.backend.api.movie.entity.Genre;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.repository.GenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.language}")
    private String language;

    public MovieService(RestTemplateBuilder builder, MovieRepository movieRepository, GenreRepository genreRepository,MovieGenreRepository movieGenreRepository) {
        this.restTemplate = builder.build();
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.movieGenreRepository = movieGenreRepository;
    }
    @Transactional
    public void fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        String url = String.format("%s/movie/%s?api_key=%s&language=ko&page=%d",
                baseUrl, type, apiKey, page);
        ResponseEntity<TmdbResponse> response =
                restTemplate.getForEntity(url, TmdbResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<TmdbMovieDto> movies = response.getBody().getResults();

            for (TmdbMovieDto dto : movies) {
                log.info("✅ 시도하는 dto.getId() = " + dto.getId());
                Optional<Movie> existing = movieRepository.findByTmdbMovieId(dto.getId());
                log.info("🔍existing.isEmpty(): " + existing.isEmpty());
                if (existing.isEmpty()) {
                    log.info("🆕 새 영화 저장: " + dto.getId() + " - " + dto.getTitle());
                    Movie movie = Movie.builder()
                            .tmdbMovieId(dto.getId())
                            .title(dto.getTitle())
                            .overview(dto.getOverview())
                            .posterPath(dto.getPosterPath())
                            .backdropPath(dto.getBackDropPath())
                            .voteAverage(dto.getVoteAverage())
                            .originalLanguage(dto.getOriginalLanguage())
                            .releaseDate(dto.getReleaseDate() != null ? dto.getReleaseDate() : null)
                            //.genreIds(dto.getGenreIds())
                            .build();
                    movieRepository.save(movie);
                    log.info("🆕 새 장르 저장: " + dto.getId() + " - " + dto.getTitle());
                    List<Long> genreIds = dto.getGenreIds();
                    List<Genre> genreEntities = genreRepository.findAllByTmdbMovieIdIn(genreIds);
                    if (genreEntities.size() != genreIds.size()) {
                        // 일부 장르가 DB에 없으면 예외 처리
                        throw new NotFoundException(ErrorStatus.NOT_FOUND_GENRE_EXCEPTION.getMessage());
                    }
                    for(Long tmdbGenreId: genreIds){

                        Genre genre = genreRepository.findByTmdbMovieId(tmdbGenreId)
                                .orElseThrow(() -> new RuntimeException("Unknown Genre: " + tmdbGenreId));

                        MovieGenre mapping = MovieGenre.builder()
                                .movie(movie)
                                .genre(genre)
                                .build();
                        movieGenreRepository.save(mapping);
                    }
                } else if (isInitial || hasChanged(existing.get(), dto)) {
                    log.info("⚠️ 중복: " + dto.getId() + " - " + dto.getTitle());
                    Movie movie = existing.get();


                    movie.updateTitle(dto.getTitle());
                    movie.updateOverview(dto.getOverview());
                    movie.updatePosterPath(dto.getPosterPath());
                    movie.updateBackDropPath(dto.getBackDropPath());
                    movie.updateVoteAverage(dto.getVoteAverage());
                    movie.updateReleaseDate(dto.getReleaseDate() != null ? dto.getReleaseDate() : null);

                    //movie.updateGenreIds(dto.getGenreIds());
                    movie.updateOriginalLanguage(dto.getOriginalLanguage());
                    movieRepository.save(movie);
                }

            }

        }
    }
    private boolean hasChanged(Movie movie, TmdbMovieDto dto) {
        return !Objects.equals(movie.getTitle(), dto.getTitle())
                || !Objects.equals(movie.getOverview(), dto.getOverview())
                || !Objects.equals(movie.getPosterPath(), dto.getPosterPath())
                || !Objects.equals(movie.getBackdropPath(), dto.getBackDropPath())
                || !Objects.equals(movie.getVoteAverage(), dto.getVoteAverage())
                //|| !Objects.equals(movie.getGenreIds(), dto.getGenreIds())
                || !Objects.equals(movie.getOriginalLanguage(), dto.getOriginalLanguage())
                || !Objects.equals(movie.getReleaseDate(),
                dto.getReleaseDate() != null ? dto.getReleaseDate() : null);
    }
    //장르 바뀌었는지
//    private boolean hasChangedGenre(Genre genre, TmdbMovieDto dto ){
//
//    }

}
