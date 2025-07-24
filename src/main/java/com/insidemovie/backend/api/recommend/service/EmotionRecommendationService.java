package com.insidemovie.backend.api.recommend.service;

import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.recommend.dto.EmotionRequestDTO;
import com.insidemovie.backend.api.recommend.dto.MovieRecommendationDTO;
import com.insidemovie.backend.api.recommend.dto.MovieSimilarityResDto;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.ExternalServiceException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionRecommendationService {

    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    private final ReviewRepository reviewRepository;
    private final RestTemplate fastApiRestTemplate;
    private final MovieRepository movieRepository;

    @Value("${spring.fastapi.url}")
    private String fastApiUrl;

    // 사용자의 감정 벡터를 기반으로 영화 추천 리스트 반환
    public List<MovieRecommendationDTO> recommendByEmotion(EmotionRequestDTO userEmotion) {
        MovieSimilarityResDto[] responseArray = fastApiRestTemplate.postForObject(
                fastApiUrl,
                userEmotion,
                MovieSimilarityResDto[].class
        );

        if (responseArray == null) {
            throw new ExternalServiceException(ErrorStatus.EXTERNAL_SERVICE_ERROR.getMessage());
        }

        List<MovieSimilarityResDto> response = Arrays.asList(responseArray);
        List<MovieRecommendationDTO> recommends = new ArrayList<>();

        for (MovieSimilarityResDto recommendMovie : response) {
            Movie movie = movieRepository.findById(recommendMovie.getMovieId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));
            MovieEmotionSummary movieEmotion = movieEmotionSummaryRepository.findByMovieId(movie.getId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EMOTION.getMessage()));

            double dominantRatio = switch (movieEmotion.getDominantEmotion()) {
                case JOY -> movieEmotion.getJoy().doubleValue();
                case SADNESS -> movieEmotion.getSadness().doubleValue();
                case ANGER -> movieEmotion.getAnger().doubleValue();
                case FEAR -> movieEmotion.getFear().doubleValue();
                case DISGUST -> movieEmotion.getDisgust().doubleValue();
                case NONE -> 0.0;
                default -> 0;
            };

            Double ratingAvg = reviewRepository.findAverageByMovieId(movieEmotion.getMovieId());
            BigDecimal rounded;
            if (ratingAvg == null || ratingAvg == 0.00) {
                rounded = BigDecimal.ZERO.setScale(2);
            } else {
                rounded = BigDecimal.valueOf(ratingAvg).setScale(2, RoundingMode.HALF_UP);
            }

            MovieRecommendationDTO dto = MovieRecommendationDTO.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .posterPath(movie.getPosterPath())
                    .voteAverage(movie.getVoteAverage())
                    .dominantEmotion(movieEmotion.getDominantEmotion())
                    .dominantEmotionRatio(dominantRatio)
                    .similarity(recommendMovie.getSimilarity())
                    .ratingAvg(rounded)
                    .build();

            recommends.add(dto);
        }

        return recommends;
    }
}
