package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieListResponse;
import com.insidemovie.backend.api.movie.service.MovieServiceKofic;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/kobis")
public class KobisController {

    private final MovieServiceKofic movieServiceKofic;

    public KobisController(MovieServiceKofic movieServiceKofic) {
        this.movieServiceKofic = movieServiceKofic;
    }
    /**
     * 영화 검색: 리액티브(Mono) 반환
     * GET /api/kobis/movies?name=기생충
     */
    @GetMapping(value = "/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MovieListResponse> getMovies(@RequestParam("name") String name) {

        return movieServiceKofic.searchMovie(name);
    }

    /**
     * 영화 검색(동기 방식)
     * GET /api/kobis/movies/blocking?name=기생충
     */
    @GetMapping(value = "/movies/blocking", produces = MediaType.APPLICATION_JSON_VALUE)
    public MovieListResponse getMoviesBlocking(@RequestParam("name") String name) {
        return movieServiceKofic.searchMovieBlocking(name);
    }
}
