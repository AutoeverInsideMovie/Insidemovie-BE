package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.service.MemberEmotionSummaryService;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieEmotionSummaryService {
    private final EmotionRepository emotionRepository;
    private final MovieEmotionSummaryRepository summaryRepository;
    private final MovieRepository movieRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MemberEmotionSummaryService memberEmotionSummaryService;
    private final MovieService movieService;

    @Transactional
    public void recalcMovieSummary(Long movieId) {
        // 1) 영화 존재 확인
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new NotFoundException("Movie not found: " + movieId));

        // 2) Emotion 테이블 평균 조회
        EmotionAvgDTO avgDto = emotionRepository
            .findAverageEmotionsByMovieId(movieId)
            .orElseGet(() -> EmotionAvgDTO.builder()
                .joy(0.0).sadness(0.0)
                .anger(0.0).fear(0.0)
                .disgust(0.0)
                .repEmotionType(EmotionType.NONE)
                .build());

        EmotionType rep = movieService.calculateRepEmotion(avgDto);
        avgDto.setRepEmotionType(rep);

        // 3) movie_emotion_summary 조회 또는 신규 생성
        MovieEmotionSummary summary = summaryRepository
            .findByMovie(movie)
            .orElseGet(() -> MovieEmotionSummary.builder()
                .movie(movie)
                .build()
            );

        // 4) 갱신 후 저장
        summary.updateFromDTO(avgDto);
        summaryRepository.save(summary);

        // 5) 이 영화를 좋아요한 모든 회원의 감정 요약 재계산
        movieLikeRepository.findByMovie(movie)
            .forEach(like -> {
                Long memberId = like.getMember().getId();
                memberEmotionSummaryService.recalcMemberSummary(memberId);
            });
    }
}