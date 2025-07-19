package com.insidemovie.backend.api.recommend.controller;

import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.recommend.dto.EmotionRequestDTO;
import com.insidemovie.backend.api.recommend.dto.MovieRecommendationDTO;
import com.insidemovie.backend.api.recommend.service.EmotionRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@Tag(name = "Recommend", description = "추천 영화 관련 API")
@RequiredArgsConstructor
public class EmotionRecommendationController {

    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    private final EmotionRecommendationService recommendationService;

    @Operation(summary = "맞춤 영화 조회", description = "감정 상태로 영화를 추천합니다")
    @PostMapping("/emotion")
    public List<MovieRecommendationDTO> recommendMovies(@RequestBody EmotionRequestDTO emotionRequest) {
        return recommendationService.recommendByEmotion(emotionRequest);
    }
}
