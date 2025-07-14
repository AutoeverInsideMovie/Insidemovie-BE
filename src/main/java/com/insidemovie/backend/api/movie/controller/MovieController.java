package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetail;
import com.insidemovie.backend.api.movie.service.MoviesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
//    private final MoviesService movieService;
//    public MovieController(MoviesService movieService) {
//        this.movieService = movieService;
//    }
//
//    // 영화 상세 정보
//    @GetMapping("/{id}")
//    public MovieDetail detail(@PathVariable String id) {
//        return movieService.getMovieDetail(id);
//    }
//
//    // 인기 영화
//    @GetMapping("/popular")
//    public List<MovieDetail> popular(@RequestParam int page) {
//        return movieService.getPopularMovies(page);
//    }
}
