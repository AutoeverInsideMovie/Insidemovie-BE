package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.entity.Genre;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.GenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MovieDetailService {
    MovieRepository movieRepository;
    GenreRepository genreRepository;

    //영화 상세 조회
    public MovieDetailResDto getMovieDetail(Long id){
        Movie movie= movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));
        List<Genre> genre = genreRepository.findByTmdbId(movie.getTmdbMovieId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_GENRE_EXCEPTION.getMessage()));


        MovieDetailResDto resDto = new MovieDetailResDto();
        resDto.setTitle(movie.getTitle());
        resDto.setOverview(movie.getOverview());
        resDto.setBackdropPath(movie.getBackdropPath());
        resDto.setPosterPath(movie.getPosterPath());
        resDto.setVoteAverage(movie.getVoteAverage());
        resDto.setOriginalLanguage(movie.getOriginalLanguage());

    }
}
