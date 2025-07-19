package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieDetailService {
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;

    //영화 상세 조회
    public MovieDetailResDto getMovieDetail(Long id){
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        List<MovieGenre> genres = movieGenreRepository.findByMovieId(movie.getId());
        if(genres.isEmpty()){
            log.error("Movie상세보기 장르 오류");
            throw new NotFoundException(ErrorStatus.NOT_FOUND_GENRE_EXCEPTION.getMessage());
        }

        List<String> genreNames= genres.stream()
                .map(mg -> mg.getGenreType().name())
                .collect(Collectors.toList());

        MovieDetailResDto resDto = new MovieDetailResDto();
        resDto.setId(movie.getId());
        resDto.setTitle(movie.getTitle());
        resDto.setOverview(movie.getOverview());
        resDto.setBackdropPath(movie.getBackdropPath());
        resDto.setPosterPath(movie.getPosterPath());
        resDto.setVoteAverage(movie.getVoteAverage());
        resDto.setOriginalLanguage(movie.getOriginalLanguage());
        resDto.setGenre(genreNames);
        return resDto;
    }

    //영화 상세 조회 - 로그인
    public MovieDetailResDto getMovieDetail(Long id, String userEmail) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        List<MovieGenre> genres = movieGenreRepository.findByMovieId(movie.getId());
        if (genres.isEmpty()) {
            log.error("Movie상세보기 장르 오류");
            throw new NotFoundException(ErrorStatus.NOT_FOUND_GENRE_EXCEPTION.getMessage());
        }

        List<String> genreNames = genres.stream()
                .map(mg ->mg.getGenreType().name())
                .collect(Collectors.toList());

        MovieDetailResDto resDto = new MovieDetailResDto();
        resDto.setId(movie.getId());
        resDto.setTitle(movie.getTitle());
        resDto.setOverview(movie.getOverview());
        resDto.setBackdropPath(movie.getBackdropPath());
        resDto.setPosterPath(movie.getPosterPath());
        resDto.setVoteAverage(movie.getVoteAverage());
        resDto.setOriginalLanguage(movie.getOriginalLanguage());
        resDto.setIsLike(movieLikeRepository.existsByMovie_IdAndMember_Id(movie.getId(), member.getId()));
        resDto.setGenre(genreNames);
        return resDto;
    }
}
